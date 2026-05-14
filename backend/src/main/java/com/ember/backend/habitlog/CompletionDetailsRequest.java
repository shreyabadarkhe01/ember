package com.ember.backend.habitlog;

import lombok.Data;

@Data
public class CompletionDetailsRequest {
    private Double completionRatio;  // 0.25, 0.75, 1.0, 1.25
    private String feelingTag;       // DRAINED, NEUTRAL, ENERGISED
}