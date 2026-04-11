package com.ember.backend.habit;

import com.ember.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository habitRepository;
    private final UserRepository userRepository;

    public HabitDto createHabit(Long userId, Habit habit) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        habit.setUser(user);
        return toDto(habitRepository.save(habit));
    }

    public List<HabitDto> getUserHabits(Long userId) {
        return habitRepository.findByUserId(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public HabitDto scaleHabit(Long habitId, int energyScore) {
        var habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        if (energyScore >= 4) {
            habit.setStatus(HabitStatus.ACTIVE);
        } else if (energyScore >= 2) {
            habit.setStatus(HabitStatus.PAUSED);
        } else {
            habit.setStatus(HabitStatus.ARCHIVED);
        }

        return toDto(habitRepository.save(habit));
    }

    public HabitDto completeHabit(Long habitId) {
        var habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));
        habit.setStreakCount(habit.getStreakCount() + 1);
        return toDto(habitRepository.save(habit));
    }

    private HabitDto toDto(Habit habit) {
        HabitDto dto = new HabitDto();
        dto.setId(habit.getId());
        dto.setName(habit.getName());
        dto.setFullVersion(habit.getFullVersion());
        dto.setLiteVersion(habit.getLiteVersion());
        dto.setMinimalVersion(habit.getMinimalVersion());
        dto.setStatus(habit.getStatus());
        dto.setStreakCount(habit.getStreakCount());
        dto.setUserId(habit.getUser().getId());
        dto.setCreatedAt(habit.getCreatedAt());
        return dto;
    }
}