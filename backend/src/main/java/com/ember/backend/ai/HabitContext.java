package com.ember.backend.ai;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Internal DTO — carries habit data into OpenAIService for nudge generation.
 * Not exposed as an API request/response type.
 */
@Data
@AllArgsConstructor
public class HabitContext {
    private String name;
    private String minimalVersion;
    private String liteVersion;
    private String fullVersion;
    private int streakCount;
}

