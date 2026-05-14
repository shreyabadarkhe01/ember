package com.ember.backend.autopsy;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class HabitWeeklySummaryDto {
    private Long habitId;
    private String habitName;
    private Integer streakCount;
    private String todayStatus;    // "DONE", "SKIPPED", "ACTIVE"
    private Integer weeklyDone;    // times marked DONE this week from HabitLog
    private Integer weeklySkipped; // times marked SKIPPED this week from HabitLog
}