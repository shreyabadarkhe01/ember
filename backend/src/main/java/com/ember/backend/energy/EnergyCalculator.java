package com.ember.backend.energy;

import com.ember.backend.biometric.BiometricDataDto;
import org.springframework.stereotype.Component;

/**
 * Calculates an energy score (1-5) from biometric data.
 *
 * Scoring breakdown (max 5 points):
 * ─────────────────────────────────────────────
 * Sleep >= 7h          → +2.0 pts (most impactful)
 * Sleep >= 5.5h        → +1.0 pt  (partial credit)
 *
 * HRV >= 50ms          → +1.5 pts (good recovery)
 * HRV >= 30ms          → +0.75 pt (partial credit)
 *
 * Resting HR <= 65 bpm → +1.0 pt  (good cardiovascular state)
 * Resting HR <= 75 bpm → +0.5 pt  (partial credit)
 *
 * Steps >= 8000        → +0.3 pt  (bonus - active day)
 * Calories >= 2000     → +0.2 pt  (bonus - active metabolism)
 * ─────────────────────────────────────────────
 * Raw score is scaled to 1-5 range.
 * If no biometric data → returns null (manual entry required)
 */
@Component
public class EnergyCalculator {

    // Thresholds
    private static final double SLEEP_GOOD = 7.0;
    private static final double SLEEP_OK   = 5.5;

    private static final double HRV_GOOD   = 50.0;
    private static final double HRV_OK     = 30.0;

    private static final int RHR_GOOD      = 65;
    private static final int RHR_OK        = 75;

    private static final int STEPS_ACTIVE  = 8000;
    private static final int CAL_ACTIVE    = 2000;

    private static final double MAX_SCORE  = 4.8; // practical max before bonus

    /**
     * Calculate energy score from biometric data.
     * @return Integer score 1-5, or null if no biometric data provided
     */
    public Integer calculate(BiometricDataDto data) {
        if (hasNoData(data)) return null;

        double score = 0.0;

        // ── Sleep (max 2.0 pts) ──────────────────────────
        if (data.getSleepHours() != null) {
            if (data.getSleepHours() >= SLEEP_GOOD) {
                score += 2.0;
            } else if (data.getSleepHours() >= SLEEP_OK) {
                score += 1.0;
            }
            // < 5.5h → 0 pts (exhausted)
        }

        // ── HRV (max 1.5 pts) ───────────────────────────
        if (data.getHrvMs() != null) {
            if (data.getHrvMs() >= HRV_GOOD) {
                score += 1.5;
            } else if (data.getHrvMs() >= HRV_OK) {
                score += 0.75;
            }
            // < 30ms → 0 pts (poor recovery)
        }

        // ── Resting Heart Rate (max 1.0 pt) ─────────────
        if (data.getRestingHeartRate() != null) {
            if (data.getRestingHeartRate() <= RHR_GOOD) {
                score += 1.0;
            } else if (data.getRestingHeartRate() <= RHR_OK) {
                score += 0.5;
            }
            // > 75 bpm → 0 pts (elevated/stressed)
        }

        // ── Bonus: Steps (max 0.3 pt) ───────────────────
        if (data.getSteps() != null && data.getSteps() >= STEPS_ACTIVE) {
            score += 0.3;
        }

        // ── Bonus: Calories (max 0.2 pt) ────────────────
        if (data.getCaloriesBurned() != null && data.getCaloriesBurned() >= CAL_ACTIVE) {
            score += 0.2;
        }

        return scaleToFive(score);
    }

    /**
     * Scale raw score to 1-5 integer.
     * Raw 0       → 1 (minimum — always show some energy)
     * Raw MAX     → 5
     */
    private int scaleToFive(double rawScore) {
        if (rawScore <= 0) return 1;
        if (rawScore >= MAX_SCORE) return 5;

        // Scale: 1 + (rawScore / MAX_SCORE) * 4
        int scaled = (int) Math.round(1 + (rawScore / MAX_SCORE) * 4);
        return Math.min(5, Math.max(1, scaled));
    }

    /**
     * Returns a breakdown of score contributions — useful for UI/debugging
     */
    public EnergyBreakdown breakdown(BiometricDataDto data) {
        EnergyBreakdown b = new EnergyBreakdown();
        if (data.getSleepHours() != null) {
            b.setSleepScore(data.getSleepHours() >= SLEEP_GOOD ? 2.0 : data.getSleepHours() >= SLEEP_OK ? 1.0 : 0.0);
            b.setSleepHours(data.getSleepHours());
        }
        if (data.getHrvMs() != null) {
            b.setHrvScore(data.getHrvMs() >= HRV_GOOD ? 1.5 : data.getHrvMs() >= HRV_OK ? 0.75 : 0.0);
            b.setHrvMs(data.getHrvMs());
        }
        if (data.getRestingHeartRate() != null) {
            b.setRhrScore(data.getRestingHeartRate() <= RHR_GOOD ? 1.0 : data.getRestingHeartRate() <= RHR_OK ? 0.5 : 0.0);
            b.setRestingHeartRate(data.getRestingHeartRate());
        }
        b.setTotalScore(calculate(data));
        return b;
    }

    private boolean hasNoData(BiometricDataDto data) {
        return data.getSleepHours() == null
                && data.getHrvMs() == null
                && data.getRestingHeartRate() == null
                && data.getSteps() == null
                && data.getCaloriesBurned() == null;
    }
}
