package com.ember.backend.checkin;

import com.ember.backend.common.AppException;
import com.ember.backend.habit.Habit;
import com.ember.backend.habit.HabitDto;
import com.ember.backend.habit.HabitMapper;
import com.ember.backend.habit.HabitRepository;
import com.ember.backend.habit.HabitStatus;
import com.ember.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final UserRepository userRepository;
    private final HabitRepository habitRepository;
    private final HabitMapper habitMapper;
    private final CheckInMapper checkInMapper;

    public CheckInDto createCheckIn(Long userId, CheckIn checkIn) {

        // 1. Validate user exists
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));


        // 2. Prevent duplicate check-in for today
        checkInRepository.findByUserIdAndCheckInDate(userId, LocalDate.now())
                .ifPresent(c -> { throw new AppException("Already checked in today", HttpStatus.BAD_REQUEST); });

        // 3. Save check-in
        checkIn.setUser(user);
        CheckIn saved = checkInRepository.save(checkIn);

        // 4. Scale all user's habits based on energy score
        List<Habit> habits = habitRepository.findByUserId(userId);
        habits.forEach(habit -> {
            if (checkIn.getEnergyScore() >= 4) {
                habit.setStatus(HabitStatus.ACTIVE);
            } else if (checkIn.getEnergyScore() >= 2) {
                habit.setStatus(HabitStatus.PAUSED);
            } else {
                habit.setStatus(HabitStatus.ARCHIVED);
            }
            habitRepository.save(habit);
        });

        // 5. Build and return response
        return toDto(saved, habits, checkIn.getEnergyScore());
    }

    public List<CheckInDto> getUserCheckIns(Long userId) {
        //to avoid N+1 query problem, we fetch all habits once and reuse the list for each check-in mapping
        List<Habit> habits = habitRepository.findByUserId(userId); // fetch ONCE

        return checkInRepository.findByUserIdOrderByCheckInDateDesc(userId)
                .stream()
                .map(c -> toDto(c, habits, c.getEnergyScore())) // reuse same list
                .collect(Collectors.toList());
    }

    public CheckInDto getTodayCheckIn(Long userId) {
        var checkIn = checkInRepository
                .findByUserIdAndCheckInDate(userId, LocalDate.now())
                .orElseThrow(() -> new AppException("No check-in found for today", HttpStatus.NOT_FOUND));
        List<Habit> habits = habitRepository.findByUserId(userId);
        return toDto(checkIn, habits, checkIn.getEnergyScore());
    }

    private String generateMessage(int energyScore) {
        return switch (energyScore) {
            case 5 -> "You're on fire today! Full send on all habits!";
            case 4 -> "Great energy! Let's make today count.";
            case 3 -> "Solid day ahead. Steady wins the race.";
            case 2 -> "Low energy day — scaled habits ready. Small steps still count.";
            default -> "Rest is part of the process. Your ember is still alive.";
        };
    }

    private CheckInDto toDto(CheckIn checkIn, List<Habit> habits, int energyScore) {
        CheckInDto dto = checkInMapper.toDto(checkIn);
        dto.setMessage(generateMessage(energyScore));
        dto.setScaledHabits(habits.stream()
                .map(habitMapper::toDto)
                .collect(Collectors.toList()));
        return dto;
    }
}