package com.ember.backend.habit;

import com.ember.backend.common.AppException;
import com.ember.backend.habitlog.HabitLog;
import com.ember.backend.habitlog.HabitLogRepository;
import com.ember.backend.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;
    private final UserRepository userRepository;
    private final HabitMapper habitMapper;

    public HabitDto createHabit(Long userId, Habit habit) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
        habit.setUser(user);
        return habitMapper.toDto(habitRepository.save(habit));
    }

    public List<HabitDto> getUserHabits(Long userId) {
        return habitRepository.findByUserId(userId)
                .stream()
                .map(habitMapper::toDto)
                .collect(Collectors.toList());
    }

    public HabitDto scaleHabit(Long habitId, int energyScore) {
        var habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new AppException("Habit not found", HttpStatus.NOT_FOUND));

        // Return the appropriate version based on energy — no status change
        String scaledVersion;
        if (energyScore <= 2) scaledVersion = habit.getMinimalVersion();
        else if (energyScore >= 4) scaledVersion = habit.getFullVersion();
        else scaledVersion = habit.getLiteVersion();

        // Just log/return — no field to store yet, frontend uses the response
        return habitMapper.toDto(habit); // HabitDto should include scaledVersion
    }

    @Transactional
    public HabitDto completeHabit(Long userId, Long habitId) {
        Habit habit = habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new AppException("Habit not found", HttpStatus.NOT_FOUND));

        habit.setStatus(HabitStatus.DONE);
        habit.setStreakCount(habit.getStreakCount() + 1);
        Habit saved = habitRepository.save(habit);

        // Log to HabitLog — only if not already logged today
        habitLogRepository.findByHabitIdAndDate(habitId, LocalDate.now())
                .ifPresentOrElse(
                        existing -> {}, // already logged, skip
                        () -> habitLogRepository.save(HabitLog.builder()
                                .userId(userId)
                                .habitId(habitId)
                                .habitName(habit.getName())
                                .date(LocalDate.now())
                                .status(HabitStatus.DONE)
                                .build())
                );

        return habitMapper.toDto(saved);
    }

    @Transactional
    public HabitDto skipHabit(Long userId, Long habitId) {
        Habit habit = habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new AppException("Habit not found", HttpStatus.NOT_FOUND));

        habit.setStatus(HabitStatus.SKIPPED);
        Habit saved = habitRepository.save(habit);

        // Log skip
        habitLogRepository.findByHabitIdAndDate(habitId, LocalDate.now())
                .ifPresentOrElse(
                        existing -> {},
                        () -> habitLogRepository.save(HabitLog.builder()
                                .userId(userId)
                                .habitId(habitId)
                                .habitName(habit.getName())
                                .date(LocalDate.now())
                                .status(HabitStatus.SKIPPED)
                                .build())
                );

        return habitMapper.toDto(saved);
    }

    public HabitDto updateHabit(Long habitId, HabitDto dto) {
        var habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new AppException("Habit not found", HttpStatus.NOT_FOUND));
        if (dto.getName() != null) habit.setName(dto.getName());
        if (dto.getFullVersion() != null) habit.setFullVersion(dto.getFullVersion());
        if (dto.getLiteVersion() != null) habit.setLiteVersion(dto.getLiteVersion());
        if (dto.getMinimalVersion() != null) habit.setMinimalVersion(dto.getMinimalVersion());
        return habitMapper.toDto(habitRepository.save(habit));
    }

    public HabitDto resetHabit(Long habitId) {
        var habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new AppException("Habit not found", HttpStatus.NOT_FOUND));
        // Decrement streak only if it was DONE
        if (habit.getStatus() == HabitStatus.DONE && habit.getStreakCount() > 0) {
            habit.setStreakCount(habit.getStreakCount() - 1);
        }
        habit.setStatus(HabitStatus.ACTIVE);
        return habitMapper.toDto(habitRepository.save(habit));
    }

    @Transactional
    public HabitDto archiveHabit(Long userId, Long habitId) {
        Habit habit = habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new AppException("Habit not found", HttpStatus.NOT_FOUND));

        habit.setStatus(HabitStatus.ARCHIVED);
        Habit saved = habitRepository.save(habit);
        return habitMapper.toDto(saved);
    }

    @Transactional
    public HabitDto unarchiveHabit(Long userId, Long habitId) {
        Habit habit = habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new AppException("Habit not found", HttpStatus.NOT_FOUND));

        habit.setStatus(HabitStatus.ACTIVE);
        Habit saved = habitRepository.save(habit);
        return habitMapper.toDto(saved);
    }
}