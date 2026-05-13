package com.ember.backend.habitlog;

import com.ember.backend.habit.HabitStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class HabitLogDto {
    private Long id;
    private Long habitId;
    private String habitName;
    private LocalDate date;
    private HabitStatus status;
    private Double completionRatio;
    private String feelingTag;
    private Integer energyScore;
}