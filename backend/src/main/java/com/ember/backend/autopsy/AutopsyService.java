package com.ember.backend.autopsy;

//import com.ember.backend.ai.ClaudeService;
import com.ember.backend.ai.OpenAIService;
import com.ember.backend.checkin.CheckIn;
import com.ember.backend.checkin.CheckInRepository;
import com.ember.backend.common.AppException;
import com.ember.backend.habit.Habit;
import com.ember.backend.habit.HabitRepository;
import com.ember.backend.habit.HabitStatus;
import com.ember.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.ember.backend.habitlog.HabitLog;
import com.ember.backend.habitlog.HabitLogRepository;
import java.util.stream.Collectors;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutopsyService {

    private final CheckInRepository checkInRepository;
    private final HabitRepository habitRepository;
    private final UserRepository userRepository;
    private final OpenAIService openAIService;
    private final HabitLogRepository habitLogRepository;

    /**
     * Generate a full weekly autopsy report for a user.
     * Analyses the last 7 days of check-ins and habits.
     */
    public AutopsyDto generateWeeklyAutopsy(Long userId) {

        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));


        // Date range — last 7 days
        LocalDate weekEnd = LocalDate.now();
        LocalDate weekStart = weekEnd.minusDays(6);

        // Fetch check-ins for the week
        List<CheckIn> checkIns = checkInRepository
                .findByUserIdAndCheckInDateBetweenOrderByCheckInDateAsc(userId, weekStart, weekEnd);

        // Fetch habits for the user
        List<Habit> habits = habitRepository.findByUserId(userId);

        // ── Build daily breakdown ────────────────────────────────────
        List<HabitLog> weekLogs = habitLogRepository.findByUserIdAndDateBetween(userId, weekStart, weekEnd);
        List<DailyEnergyDto> energyByDay = buildDailyBreakdown(weekStart, weekEnd, checkIns, weekLogs);

        // ── Energy analysis ──────────────────────────────────────────

        List<Integer> scores = checkIns.stream()
                .map(CheckIn::getEnergyScore)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        double avgEnergy = scores.isEmpty() ? 0.0 :
                scores.stream().mapToInt(i -> i).average().orElse(0.0);

        // Best and worst days
        String bestDay = findDayByScore(checkIns, true);
        String worstDay = findDayByScore(checkIns, false);

        long highEnergyDays = scores.stream().filter(s -> s >= 4).count();
        long lowEnergyDays = scores.stream().filter(s -> s <= 2).count();

        // ── Consistency ──────────────────────────────────────────────
        int totalCheckIns = (int) checkIns.stream()
                .map(CheckIn::getCheckInDate)
                .distinct()
                .count();
        int consistencyScore = (int) Math.round((totalCheckIns / 7.0) * 100);

        // ── Habit performance ────────────────────────────────────────
        // Count habits by status across all check-ins this week

        // Per-habit weekly summary
        List<HabitWeeklySummaryDto> habitSummaries = habits.stream()
                .filter(h -> h.getStatus() != HabitStatus.ARCHIVED)
                .map(h -> {
                    int done = (int) weekLogs.stream()
                            .filter(l -> l.getHabitId().equals(h.getId())
                                    && l.getStatus() == HabitStatus.DONE)
                            .count();
                    int skipped = (int) weekLogs.stream()
                            .filter(l -> l.getHabitId().equals(h.getId())
                                    && l.getStatus() == HabitStatus.SKIPPED)
                            .count();
                    return HabitWeeklySummaryDto.builder()
                            .habitId(h.getId())
                            .habitName(h.getName())
                            .streakCount(h.getStreakCount())
                            .todayStatus(h.getStatus().name())
                            .weeklyDone(done)
                            .weeklySkipped(skipped)
                            .build();
                })
                .collect(Collectors.toList());

        long totalLogs = weekLogs.size();
        long doneLogs = weekLogs.stream().filter(l -> l.getStatus() == HabitStatus.DONE).count();
        long skippedLogs = weekLogs.stream().filter(l -> l.getStatus() == HabitStatus.SKIPPED).count();

        long attemptedLogs = doneLogs + skippedLogs;
        double habitCompletionRate = attemptedLogs > 0
                ? Math.round((doneLogs * 100.0 / attemptedLogs) * 10.0) / 10.0
                : 0.0;

        int activeHabitCount = (int) habits.stream()
                .filter(h -> h.getStatus() != HabitStatus.ARCHIVED)
                .count();
        int totalHabits = totalCheckIns * activeHabitCount; // total possible completions this week

        // ── Pattern detection ────────────────────────────────────────
        List<String> energyPatterns = detectPatterns(checkIns, avgEnergy);
        List<String> habitPatterns = detectHabitPatterns(userId, weekStart, weekEnd, checkIns, habits);

        List<String> allPatterns = new ArrayList<>();
        allPatterns.addAll(habitPatterns);
        allPatterns.add("__DIVIDER__"); // frontend uses this to split sections
        allPatterns.addAll(energyPatterns);

        // ── Biometric correlations ───────────────────────────────────
        String sleepCorrelation = analyseSleepCorrelation(checkIns);
        String hrvCorrelation = analyseHrvCorrelation(checkIns);

        // ── Week summary label ───────────────────────────────────────
        String weekSummary = generateWeekSummary(avgEnergy, consistencyScore);

        AutopsyDto autopsy = AutopsyDto.builder()
                .userId(userId)
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .avgEnergyScore(Math.round(avgEnergy * 10.0) / 10.0)
                .bestDay(bestDay)
                .worstDay(worstDay)
                .highEnergyDays((int) highEnergyDays)
                .lowEnergyDays((int) lowEnergyDays)
                .totalCheckIns(totalCheckIns)
                .consistencyScore(consistencyScore)
                .habitCompletionRate((int) Math.round(habitCompletionRate))
                .totalHabitsDone((int) doneLogs)
                .totalHabitsSkipped((int) skippedLogs)
                .habitSummaries(habitSummaries)
                .activeHabitCount(activeHabitCount)
                .energyByDay(energyByDay)
                .patterns(allPatterns)
                .sleepCorrelation(sleepCorrelation)
                .hrvCorrelation(hrvCorrelation)
                .weekSummary(weekSummary)
                .aiInsight(null)
                .build();

        try {
            String userName = userRepository.findById(userId)
                    .get().getName().split(" ")[0];
            String insight = openAIService.generateAutopsyInsight(userName, autopsy);
            autopsy.setAiInsight(insight);
        } catch (Exception e) {
            log.warn("Claude insight generation failed, continuing without it");
        }


        return autopsy;
    }

    // ── Private helpers ──────────────────────────────────────────────

    /**
     * Build a DailyEnergyDto for each day in the week,
     * including day where user didn't check in (checkedIn = false).
     */

    private List<DailyEnergyDto> buildDailyBreakdown(
            LocalDate weekStart, LocalDate weekEnd, List<CheckIn> checkIns,
            List<HabitLog> weekLogs) {

        Map<LocalDate, CheckIn> checkInMap = checkIns.stream()
                .collect(Collectors.toMap(
                        CheckIn::getCheckInDate,
                        c -> c,
                        (existing, replacement) -> replacement // keeps latest if duplicate
                ));

        // Group habit logs by date — only DONE ones
        Map<LocalDate, List<String>> habitsDoneByDate = weekLogs.stream()
                .filter(l -> l.getStatus() == HabitStatus.DONE && l.getHabitName() != null)
                .collect(Collectors.groupingBy(
                        HabitLog::getDate,
                        Collectors.mapping(HabitLog::getHabitName, Collectors.toList())
                ));

        List<DailyEnergyDto> days = new ArrayList<>();
        LocalDate current = weekStart;

        while (!current.isAfter(weekEnd)) {
            CheckIn checkIn = checkInMap.get(current);
            String dayName = current.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            DailyEnergyDto.DailyEnergyDtoBuilder builder = DailyEnergyDto.builder()
                    .date(current)
                    .dayName(dayName)
                    .checkedIn(checkIn != null)
                    .habitsDone(habitsDoneByDate.getOrDefault(current, List.of()));

            if (checkIn != null) {
                builder.energyScore(checkIn.getEnergyScore())
                        .source(checkIn.getSource())
                        .sleepHours(checkIn.getSleepHours())
                        .restingHeartRate(checkIn.getRestingHeartRate())
                        .steps(checkIn.getSteps());
            }

            days.add(builder.build());
            current = current.plusDays(1);
        }

        return days;
    }

    /**
     * Find the day name with highest (best=true) or lowest (best=false) energy score.
     */
    private String findDayByScore(List<CheckIn> checkIns, boolean best) {
        if (checkIns.isEmpty()) return "N/A";

        return checkIns.stream()
                .filter(c -> c.getEnergyScore() != null)
                .min(Comparator.comparingInt(c ->
                        best ? -c.getEnergyScore() : c.getEnergyScore()))
                .map(c -> c.getCheckInDate().getDayOfWeek()
                        .getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                .orElse("N/A");
    }

    /**
     * Detect patterns in the week's data.
     * Returns human-readable strings shown in the autopsy report.
     */
    private List<String> detectPatterns(List<CheckIn> checkIns, double avgEnergy) {
        List<String> patterns = new ArrayList<>();

        if (checkIns.isEmpty()) {
            patterns.add("No check-ins this week — start checking in daily for pattern analysis");
            return patterns;
        }

        // Pattern: Consistently low energy
        long lowCount = checkIns.stream()
                .filter(c -> c.getEnergyScore() != null && c.getEnergyScore() <= 2)
                .count();
        if (lowCount >= 3) {
            patterns.add("⚠️ Low energy on " + lowCount + " days this week — consider reviewing sleep or workload");
        }

        // Pattern: Consistently high energy
        long highCount = checkIns.stream()
                .filter(c -> c.getEnergyScore() != null && c.getEnergyScore() >= 4)
                .count();
        if (highCount >= 4) {
            patterns.add("🔥 Strong energy on " + highCount + " days — great recovery this week!");
        }

        // Pattern: Weekend energy drop
        boolean weekendLow = checkIns.stream()
                .filter(c -> {
                    DayOfWeek day = c.getCheckInDate().getDayOfWeek();
                    return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
                })
                .anyMatch(c -> c.getEnergyScore() != null && c.getEnergyScore() <= 2);
        if (weekendLow) {
            patterns.add("📉 Energy dips on weekends — weekend routine may need adjustment");
        }

        // Pattern: Monday low energy (common pattern)
        checkIns.stream()
                .filter(c -> c.getCheckInDate().getDayOfWeek() == DayOfWeek.MONDAY)
                .filter(c -> c.getEnergyScore() != null && c.getEnergyScore() <= 2)
                .findFirst()
                .ifPresent(c -> patterns.add("😴 Low energy on Monday — weekend sleep schedule may be off"));

        // Pattern: Missed check-ins
        if (checkIns.size() < 4) {
            patterns.add("📋 Only " + checkIns.size() + "/7 days tracked — more data needed for accurate patterns");
        }

        // Pattern: Sleep correlation (if sleep data available)
        long goodSleepHighEnergy = checkIns.stream()
                .filter(c -> c.getSleepHours() != null && c.getSleepHours() >= 7.0
                        && c.getEnergyScore() != null && c.getEnergyScore() >= 4)
                .count();
        if (goodSleepHighEnergy >= 2) {
            patterns.add("💤 Good sleep (7h+) strongly correlates with high energy days for you");
        }

        // Pattern: Energy improving or declining across the week
        if (checkIns.size() >= 4) {
            List<Integer> scores = checkIns.stream()
                    .map(CheckIn::getEnergyScore)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            int firstHalf = scores.subList(0, scores.size() / 2).stream()
                    .mapToInt(i -> i).sum();
            int secondHalf = scores.subList(scores.size() / 2, scores.size()).stream()
                    .mapToInt(i -> i).sum();

            if (secondHalf > firstHalf + 2) {
                patterns.add("📈 Energy improved as the week progressed — good momentum!");
            } else if (firstHalf > secondHalf + 2) {
                patterns.add("📉 Energy declined through the week — fatigue may be building up");
            }
        }

        if (patterns.isEmpty()) {
            patterns.add("✅ Steady week — no significant patterns detected");
        }

        return patterns;
    }

    private List<String> detectHabitPatterns(Long userId, LocalDate from, LocalDate to,
                                             List<CheckIn> checkIns, List<Habit> habits) {
        List<String> patterns = new ArrayList<>();

        final List<HabitLog> logs = habitLogRepository.findByUserIdAndDateBetween(userId, from, to);

        if (logs.isEmpty()) {
            patterns.add("📊 No habit activity logged this week yet");
            return patterns;
        }

        // Build habit name lookup map from habits list — fixes "Unknown habit"
        Map<Long, String> habitNameMap = habits.stream()
                .collect(Collectors.toMap(Habit::getId, Habit::getName));

        // Group by habit
        Map<Long, List<HabitLog>> byHabit = logs.stream()
                .collect(Collectors.groupingBy(HabitLog::getHabitId));

        String bestHabit = null;
        int bestDone = 0;
        String worstHabit = null;
        int worstSkipped = 0;

        for (Map.Entry<Long, List<HabitLog>> entry : byHabit.entrySet()) {
            List<HabitLog> habitLogs = entry.getValue();
            // Use name lookup map instead of getHabitName()
            String name = habitNameMap.getOrDefault(entry.getKey(), "Habit #" + entry.getKey());

            long done = habitLogs.stream()
                    .filter(l -> l.getStatus() == HabitStatus.DONE).count();
            long skipped = habitLogs.stream()
                    .filter(l -> l.getStatus() == HabitStatus.SKIPPED).count();

            if (done > bestDone) {
                bestDone = (int) done;
                bestHabit = name;
            }

            if (skipped > worstSkipped) {
                worstSkipped = (int) skipped;
                worstHabit = name;
            }

            if (skipped >= 3) {
                patterns.add("📉 " + name + " skipped " + skipped + "x this week — consider a lighter version");
            } else if (skipped == 2) {
                patterns.add("⚠️ " + name + " skipped twice this week — streak at risk");
            }
        }

        // Best habit — positive reinforcement
        if (bestHabit != null && bestDone >= 3) {
            patterns.add(0, "🎯 " + bestHabit + " is your strongest habit — completed " + bestDone + "x this week");
        }

        // Worst habit
        if (worstHabit != null && worstSkipped >= 3) {
            patterns.add("🔴 " + worstHabit + " needs the most attention this week");
        }

        // Energy-habit correlation — build energy map
        Map<LocalDate, Integer> energyByDate = checkIns.stream()
                .filter(c -> c.getEnergyScore() != null)
                .collect(Collectors.toMap(
                        CheckIn::getCheckInDate,
                        CheckIn::getEnergyScore,
                        (a, b) -> a
                ));

        // Collect habits consistently skipped on low energy — deduplicated into one insight
        List<String> lowEnergySkippers = new ArrayList<>();
        for (Map.Entry<Long, List<HabitLog>> entry : byHabit.entrySet()) {
            List<HabitLog> habitLogs = entry.getValue();
            String name = habitNameMap.getOrDefault(entry.getKey(), "Habit #" + entry.getKey());

            long skippedOnLowEnergy = habitLogs.stream()
                    .filter(l -> l.getStatus() == HabitStatus.SKIPPED)
                    .filter(l -> {
                        Integer energy = energyByDate.get(l.getDate());
                        return energy != null && energy <= 2;
                    })
                    .count();

            long doneOnLowEnergy = habitLogs.stream()
                    .filter(l -> l.getStatus() == HabitStatus.DONE)
                    .filter(l -> {
                        Integer energy = energyByDate.get(l.getDate());
                        return energy != null && energy <= 2;
                    })
                    .count();

            if (skippedOnLowEnergy >= 2 && doneOnLowEnergy == 0) {
                lowEnergySkippers.add(name);
            }
        }

        // Single deduplicated insight instead of one per habit
        if (lowEnergySkippers.size() == 1) {
            patterns.add("🔗 " + lowEnergySkippers.get(0) + " is consistently skipped on low energy days — consider a lighter minimal version");
        } else if (lowEnergySkippers.size() > 1) {
            patterns.add("🔗 " + lowEnergySkippers.size() + " habits are consistently skipped on low energy days — minimal versions need review");
        }

        // Sleep correlation
        List<HabitLog> doneAfterGoodSleep = logs.stream()
                .filter(l -> l.getStatus() == HabitStatus.DONE)
                .filter(l -> {
                    CheckIn ci = checkIns.stream()
                            .filter(c -> c.getCheckInDate().equals(l.getDate()))
                            .findFirst().orElse(null);
                    return ci != null && ci.getSleepHours() != null && ci.getSleepHours() >= 7.0;
                })
                .collect(Collectors.toList());

        List<HabitLog> doneAfterPoorSleep = logs.stream()
                .filter(l -> l.getStatus() == HabitStatus.DONE)
                .filter(l -> {
                    CheckIn ci = checkIns.stream()
                            .filter(c -> c.getCheckInDate().equals(l.getDate()))
                            .findFirst().orElse(null);
                    return ci != null && ci.getSleepHours() != null && ci.getSleepHours() < 6.0;
                })
                .collect(Collectors.toList());

        if (!doneAfterGoodSleep.isEmpty() && doneAfterGoodSleep.size() > doneAfterPoorSleep.size() + 1) {
            patterns.add("💤 You complete significantly more habits after 7+ hours of sleep — sleep is your biggest lever");
        }

        // HRV correlation
        long highHrvDone = logs.stream()
                .filter(l -> l.getStatus() == HabitStatus.DONE)
                .filter(l -> {
                    CheckIn ci = checkIns.stream()
                            .filter(c -> c.getCheckInDate().equals(l.getDate()))
                            .findFirst().orElse(null);
                    return ci != null && ci.getHrvMs() != null && ci.getHrvMs() >= 50;
                })
                .count();

        long lowHrvSkipped = logs.stream()
                .filter(l -> l.getStatus() == HabitStatus.SKIPPED)
                .filter(l -> {
                    CheckIn ci = checkIns.stream()
                            .filter(c -> c.getCheckInDate().equals(l.getDate()))
                            .findFirst().orElse(null);
                    return ci != null && ci.getHrvMs() != null && ci.getHrvMs() < 40;
                })
                .count();

        if (highHrvDone >= 2) {
            patterns.add("❤️ High HRV days correlate with better habit completion — recovery is working");
        }
        if (lowHrvSkipped >= 2) {
            patterns.add("📡 Low HRV days see more skipped habits — your body signals are reliable");
        }

        return patterns;
    }
    /**
     * Analyse correlation between sleep and energy score.
     */
    private String analyseSleepCorrelation(List<CheckIn> checkIns) {
        List<CheckIn> withSleep = checkIns.stream()
                .filter(c -> c.getSleepHours() != null && c.getEnergyScore() != null)
                .collect(Collectors.toList());

        if (withSleep.size() < 3) return "Not enough sleep data tracked this week";

        long highSleepHighEnergy = withSleep.stream()
                .filter(c -> c.getSleepHours() >= 7.0 && c.getEnergyScore() >= 4)
                .count();
        long lowSleepLowEnergy = withSleep.stream()
                .filter(c -> c.getSleepHours() < 6.0 && c.getEnergyScore() <= 2)
                .count();

        if (highSleepHighEnergy + lowSleepLowEnergy >= 2) {
            return "Strong correlation — sleep directly affects your energy score";
        }
        return "Moderate correlation — sleep is one of several factors";
    }

    /**
     * Analyse HRV correlation if data available.
     */
    private String analyseHrvCorrelation(List<CheckIn> checkIns) {
        long withHrv = checkIns.stream()
                .filter(c -> c.getHrvMs() != null)
                .count();

        if (withHrv == 0) return "HRV not tracked this week — connect Samsung Health for deeper insights";

        long highHrvHighEnergy = checkIns.stream()
                .filter(c -> c.getHrvMs() != null && c.getHrvMs() >= 50
                        && c.getEnergyScore() != null && c.getEnergyScore() >= 4)
                .count();

        if (highHrvHighEnergy >= 2) {
            return "High HRV days align with high energy — good recovery response";
        }
        return "HRV tracked on " + withHrv + " days — more data needed for correlation";
    }

    /**
     * Generate a one-line week summary based on avg energy and consistency.
     */
    private String generateWeekSummary(double avgEnergy, int consistencyScore) {
        if (consistencyScore < 40) return "Missed week 😶 — try to check in daily";
        if (avgEnergy >= 4.0 && consistencyScore >= 80) return "Exceptional week 🔥";
        if (avgEnergy >= 3.5 && consistencyScore >= 70) return "Strong week 💪";
        if (avgEnergy >= 3.0 && consistencyScore >= 60) return "Solid week 😊";
        if (avgEnergy >= 2.5) return "Average week 😐 — room to improve";
        return "Recovery week 😴 — rest and reset";
    }

   



}
