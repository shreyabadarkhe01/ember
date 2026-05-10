package com.ember.backend.ai;

import lombok.Data;

/**
 * Request body for POST /api/users/{id}/ai/nudge
 * Frontend only needs to send energy data — habits are fetched server-side.
 */
@Data
public class NudgeRequest {
    private int energyScore;        // 1–5, required
    private Double sleepHours;      // nullable
    private Integer hrvMs;          // nullable
}
