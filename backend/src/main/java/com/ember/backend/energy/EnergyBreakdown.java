package com.ember.backend.energy;

import lombok.Data;

/**
 * Shows how the energy score was calculated — returned to frontend
 * so user understands why they got score X.
 *
 * Example response:
 * {
 *   "totalScore": 4,
 *   "sleepHours": 7.5,   "sleepScore": 2.0,
 *   "hrvMs": 55.0,       "hrvScore": 1.5,
 *   "restingHeartRate": 62, "rhrScore": 1.0,
 *   "summary": "Great sleep and recovery → High energy day 🔥"
 * }
 */
@Data
public class EnergyBreakdown {

    private Integer totalScore;

    private Double sleepHours;
    private Double sleepScore;

    private Double hrvMs;
    private Double hrvScore;

    private Integer restingHeartRate;
    private Double rhrScore;

    private Integer steps;
    private Integer caloriesBurned;

    // Human readable summary — shown in app UI
    public String getSummary() {
        if (totalScore == null) return "No biometric data available";
        return switch (totalScore) {
            case 1 -> "Your body needs rest today 😴 — light habits only";
            case 2 -> "Low energy detected 😔 — gentle habits recommended";
            case 3 -> "Moderate recovery 😐 — standard habits today";
            case 4 -> "Good energy levels 😊 — full habits on track";
            case 5 -> "Excellent recovery 🔥 — challenge mode activated";
            default -> "Energy calculated";
        };
    }
}
