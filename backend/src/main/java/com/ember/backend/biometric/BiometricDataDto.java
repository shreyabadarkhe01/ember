package com.ember.backend.biometric;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for biometric data sent from:
 * - Flutter app (Samsung Health Data SDK auto-populated)
 * - Web app (manual entry fallback)
 *
 * All fields are optional individually — energy score is calculated
 * from whichever fields are available.
 */
@Data
public class BiometricDataDto {

    // Sleep duration in hours (e.g. 7.5)
    // Samsung Health: SleepSessionType
    @DecimalMin(value = "0.0") @DecimalMax(value = "24.0")
    private Double sleepHours;

    // Heart Rate Variability in milliseconds (e.g. 55.0)
    // Samsung Health: HeartRateVariabilityMsdType
    @DecimalMin(value = "0.0")
    private Double hrvMs;

    // Resting Heart Rate in bpm (e.g. 62)
    // Samsung Health: HeartRateType (resting)
    @Min(0)
    private Integer restingHeartRate;

    // Steps count for the day (e.g. 8000)
    // Samsung Health: StepCountType
    @Min(0)
    private Integer steps;

    // Calories burned (e.g. 2200)
    // Samsung Health: NutritionType / CaloriesBurnedType
    @Min(0)
    private Integer caloriesBurned;

    // Source of this data — for tracking/debugging
    // Values: "samsung_health", "manual", "health_connect"
    @NotNull
    private String source;

    // Optional note from user
    private String note;
}
