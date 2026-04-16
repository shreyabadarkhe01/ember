package com.ember.backend.habit;

import com.ember.backend.common.AppException;
import com.ember.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository habitRepository;
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

        if (energyScore >= 4) {
            habit.setStatus(HabitStatus.ACTIVE);
        } else if (energyScore >= 2) {
            habit.setStatus(HabitStatus.PAUSED);
        } else {
            habit.setStatus(HabitStatus.ARCHIVED);
        }

        return habitMapper.toDto(habitRepository.save(habit));
    }

    public HabitDto completeHabit(Long habitId) {
        var habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new AppException("Habit not found", HttpStatus.NOT_FOUND));
        habit.setStreakCount(habit.getStreakCount() + 1);
        return habitMapper.toDto(habitRepository.save(habit));
    }
}