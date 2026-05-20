package com.ember.backend.autopsy;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Energy score for a single day — used in the weekly chart.
 */
@Data
@Builder
public class DailyEnergyDto {

    private LocalDate date;
    private String dayName;        // "Monday", "Tuesday" etc.
    private Integer energyScore;   // 1-5, null if no check-in that day
    private Boolean checkedIn;     // false if user missed this day
    private String source;         // "samsung_health", "manual", "health_connect"

    // Biometric snapshot for the day
    private Double sleepHours;
    private Double hrvMs;
    private Integer restingHeartRate;
    private Integer steps;
    private List<String> habitsDone;
}
