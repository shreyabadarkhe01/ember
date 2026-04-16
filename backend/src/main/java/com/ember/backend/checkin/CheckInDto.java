package com.ember.backend.checkin;

import com.ember.backend.habit.HabitDto;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CheckInDto {
    private Long id;
    private Integer energyScore;
    private Double sleepHours;
    private String notes;
    private LocalDate checkInDate;
    private Long userId;
    private String message;
    private List<HabitDto> scaledHabits;
}