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
        log.info("Date range: {} to {}", weekStart, weekEnd);

        // Fetch check-ins for the week
        List<CheckIn> checkIns = checkInRepository
                .findByUserIdAndCheckInDateBetweenOrderByCheckInDateAsc(userId, weekStart, weekEnd);
        log.info("CheckIns found: {}", checkIns.size());

        // Fetch habits for the user
        List<Habit> habits = habitRepository.findByUserId(userId);
        log.info("Habits found: {}", habits.size());

        // ── Build daily breakdown ────────────────────────────────────
        log.info("Daily breakdown built");
        List<DailyEnergyDto> energyByDay = buildDailyBreakdown(weekStart, weekEnd, checkIns);
        log.info("Daily breakdown done");

        log.info("Building AutopsyDto...");

        // ── Energy analysis ──────────────────────────────────────────
        log.info("Calculating scores...");
        List<Integer> scores = checkIns.stream()
                .map(CheckIn::getEnergyScore)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        log.info("Scores: {}", scores);

        double avgEnergy = scores.isEmpty() ? 0.0 :
                scores.stream().mapToInt(i -> i).average().orElse(0.0);
        log.info("Avg energy: {}", avgEnergy);

        // Best and worst days
        log.info("Finding best day...");
        String bestDay = findDayByScore(checkIns, true);
        log.info("Best day: {}", bestDay);
        log.info("Finding worst day...");
        String worstDay = findDayByScore(checkIns, false);
        log.info("Worst day: {}", worstDay);

        log.info("Counting high/low energy days...");
        long highEnergyDays = scores.stream().filter(s -> s >= 4).count();
        long lowEnergyDays = scores.stream().filter(s -> s <= 2).count();
        log.info("High: {}, Low: {}", highEnergyDays, lowEnergyDays);

        // ── Consistency ──────────────────────────────────────────────
        log.info("Consistency...");
        int totalCheckIns = (int) checkIns.stream()
                .map(CheckIn::getCheckInDate)
                .distinct()
                .count();
        int consistencyScore = (int) Math.round((totalCheckIns / 7.0) * 100);

        // ── Habit performance ────────────────────────────────────────
        // Count habits by status across all check-ins this week

        log.info("Habit performance...");
        LocalDate to   = LocalDate.now();
        LocalDate from = to.minusDays(6); // last 7 days

        List<HabitLog> logs = habitLogRepository.findByUserIdAndDateBetween(userId, from, to);

        // Per-habit weekly summary
        List<HabitWeeklySummaryDto> habitSummaries = habits.stream()
                .filter(h -> h.getStatus() != HabitStatus.ARCHIVED)
                .map(h -> {
                    int done = (int) logs.stream()
                            .filter(l -> l.getHabitId().equals(h.getId())
                                    && l.getStatus() == HabitStatus.DONE)
                            .count();
                    int skipped = (int) logs.stream()
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

        long totalLogs  = logs.size();
        long doneLogs   = logs.stream().filter(l -> l.getStatus() == HabitStatus.DONE).count();
        long skippedLogs = logs.stream().filter(l -> l.getStatus() == HabitStatus.SKIPPED).count();

        long attemptedLogs = doneLogs + skippedLogs;
        double habitCompletionRate = attemptedLogs > 0
                ? Math.round((doneLogs * 100.0 / attemptedLogs) * 10.0) / 10.0
                : 0.0;

        int activeHabitCount = (int) habits.stream()
                .filter(h -> h.getStatus() != HabitStatus.ARCHIVED)
                .count();
        int totalHabits = totalCheckIns * activeHabitCount; // total possible completions this week
//        log.info("Done habits: {}", doneLogs);

        // ── Pattern detection ────────────────────────────────────────
        log.info("Detecting patterns...");
        List<String> energyPatterns = detectPatterns(checkIns, avgEnergy);
        List<String> habitPatterns = detectHabitPatterns(userId, weekStart, weekEnd, checkIns);

        List<String> allPatterns = new ArrayList<>();
        allPatterns.addAll(habitPatterns);
        allPatterns.add("__DIVIDER__"); // frontend uses this to split sections
        allPatterns.addAll(energyPatterns);
        log.info("Patterns done");

        // ── Biometric correlations ───────────────────────────────────
        log.info("Sleep correlation...");
        String sleepCorrelation = analyseSleepCorrelation(checkIns);
        log.info("Sleep done");

        log.info("HRV correlation...");
        String hrvCorrelation = analyseHrvCorrelation(checkIns);
        log.info("HRV done");

        // ── Week summary label ───────────────────────────────────────
        log.info("Week summary...");
        String weekSummary = generateWeekSummary(avgEnergy, consistencyScore);
        log.info("Building final DTO...");

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
     * including days where user didn't check in (checkedIn = false).
     */

    private List<DailyEnergyDto> buildDailyBreakdown(
            LocalDate weekStart, LocalDate weekEnd, List<CheckIn> checkIns) {

        log.info("Building map...");
        Map<LocalDate, CheckIn> checkInMap = checkIns.stream()
                .collect(Collectors.toMap(
                        CheckIn::getCheckInDate,
                        c -> c,
                        (existing, replacement) -> replacement // keep latest if duplicate
                ));
        log.info("Map built: {}", checkInMap.size());

        List<DailyEnergyDto> days = new ArrayList<>();
        LocalDate current = weekStart;

        while (!current.isAfter(weekEnd)) {
            log.info("Processing day: {}", current);
            CheckIn checkIn = checkInMap.get(current);
            String dayName = current.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            DailyEnergyDto.DailyEnergyDtoBuilder builder = DailyEnergyDto.builder()
                    .date(current)
                    .dayName(dayName)
                    .checkedIn(checkIn != null);

            if (checkIn != null) {
                log.info("CheckIn found for {}: energy={}", current, checkIn.getEnergyScore());
                builder.energyScore(checkIn.getEnergyScore())
                        .source(checkIn.getSource());
            }

            days.add(builder.build());
            current = current.plusDays(1);
        }

        log.info("Daily breakdown complete: {} days", days.size());
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

    private List<String> detectHabitPatterns(Long userId, LocalDate from, LocalDate to, List<CheckIn> checkIns) {
        List<String> patterns = new ArrayList<>();

        List<HabitLog> logs = habitLogRepository.findByUserIdAndDateBetween(userId, from, to);

        if (logs.isEmpty()) {
            patterns.add("📊 No habit activity logged this week yet");
            return patterns;
        }

        // ── Group by habit ────────────────────────────────────────────────
        Map<Long, List<HabitLog>> byHabit = logs.stream()
                .collect(Collectors.groupingBy(HabitLog::getHabitId));

        String bestHabit = null;
        int bestDone = 0;
        String worstHabit = null;
        int worstSkipped = 0;

        for (Map.Entry<Long, List<HabitLog>> entry : byHabit.entrySet()) {
            List<HabitLog> habitLogs = entry.getValue();
            String name = habitLogs.get(0).getHabitName() != null
                    ? habitLogs.get(0).getHabitName() : "Unknown habit";

            long done = habitLogs.stream()
                    .filter(l -> l.getStatus() == HabitStatus.DONE).count();
            long skipped = habitLogs.stream()
                    .filter(l -> l.getStatus() == HabitStatus.SKIPPED).count();

            // Best performing
            if (done > bestDone) {
                bestDone = (int) done;
                bestHabit = name;
            }

            // Worst performing
            if (skipped > worstSkipped) {
                worstSkipped = (int) skipped;
                worstHabit = name;
            }

            if (skipped >= 3) {
                patterns.add("📉 " + name + " skipped " + skipped + "x this week — streak at risk, consider a lighter version");
            } else if (skipped == 2) {
                patterns.add("⚠️ " + name + " skipped twice this week — streak at risk");
            }
        }

        // Best habit first — positive reinforcement
        if (bestHabit != null && bestDone >= 3) {
            patterns.add(0, "🎯 " + bestHabit + " is your strongest habit — completed " + bestDone + "x this week");
        }

        // Worst habit after
        if (worstHabit != null && worstSkipped >= 3) {
            patterns.add("🔴 " + worstHabit + " needs the most attention this week");
        }

        // ── Energy-habit correlation ──────────────────────────────────────
        // Build a map of date → energyScore from check-ins
        Map<LocalDate, Integer> energyByDate = checkIns.stream()
                .filter(c -> c.getEnergyScore() != null)
                .collect(Collectors.toMap(
                        CheckIn::getCheckInDate,
                        CheckIn::getEnergyScore,
                        (a, b) -> a // keep first if duplicate date
                ));

        // For each habit, check if it's skipped on low energy days specifically
        for (Map.Entry<Long, List<HabitLog>> entry : byHabit.entrySet()) {
            List<HabitLog> habitLogs = entry.getValue();
            String name = habitLogs.get(0).getHabitName() != null
                    ? habitLogs.get(0).getHabitName() : "Unknown habit";

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
                patterns.add("🔗 " + name + " is consistently skipped on low energy days — consider a lighter minimal version");
            }
        }

        // ── Biometric correlations ────────────────────────────────────────
        // Sleep → completion rate
        List<HabitLog> doneAfterGoodSleep = logs.stream()
                .filter(l -> l.getStatus() == HabitStatus.DONE)
                .filter(l -> {
                    Integer energy = energyByDate.get(l.getDate());
                    // proxy: if energy >= 4 on that day (biometric sleep data if available)
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
            patterns.add("📡 Low HRV days see more skipped habits — your body signals are reliable stress indicators");
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
