package com.ember.backend.autopsy;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Full weekly autopsy report for a user.
 *
 * Returned by GET /api/users/{id}/autopsy
 *
 * Example response:
 * {
 *   "userId": 1,
 *   "weekStart": "2026-04-21",
 *   "weekEnd": "2026-04-27",
 *   "avgEnergyScore": 3.4,
 *   "bestDay": "Monday",
 *   "worstDay": "Wednesday",
 *   "consistencyScore": 85,
 *   "habitCompletionRate": 68,
 *   "totalCheckIns": 6,
 *   "energyByDay": [...],
 *   "patterns": [...],
 *   "aiInsight": "Claude generated insight here"
 * }
 */
@Data
@Builder
public class AutopsyDto {

    private Long userId;
    private LocalDate weekStart;
    private LocalDate weekEnd;

    // ── Energy Analysis ──────────────────────────────
    private Double avgEnergyScore;       // average energy across checked-in days
    private String bestDay;              // day with highest energy e.g. "Monday"
    private String worstDay;             // day with lowest energy e.g. "Wednesday"
    private Integer highEnergyDays;      // days with score >= 4
    private Integer lowEnergyDays;       // days with score <= 2

    // ── Consistency ──────────────────────────────────
    private Integer totalCheckIns;       // how many days user checked in (out of 7)
    private Integer consistencyScore;    // percentage e.g. 85 means 6/7 days

    // ── Habit Performance ────────────────────────────
    private Integer habitCompletionRate; // percentage of habits marked DONE
    private Integer totalHabitsDone;     // habits marked DONE
    private Integer totalHabitsSkipped;  // habits marked SKIPPED
    private List<HabitWeeklySummaryDto> habitSummaries; // per-habit weekly breakdown
    private Integer activeHabitCount; // habits excluding archived, for label context

    // ── Daily Breakdown ──────────────────────────────
    private List<DailyEnergyDto> energyByDay; // energy score per day

    // ── Patterns detected ────────────────────────────
    private List<String> patterns;       // human readable pattern strings

    // ── Biometric Correlations ───────────────────────
    private String sleepCorrelation;     // e.g. "High sleep → High energy (strong)"
    private String hrvCorrelation;       // e.g. "HRV not tracked this week"

    // ── AI Insight (populated by ClaudeService) ──────
    private String aiInsight;           // Claude generated insight and recommendation

    // ── Summary label ────────────────────────────────
    private String weekSummary;         // e.g. "Strong week 🔥" or "Recovery week 😴"
}
