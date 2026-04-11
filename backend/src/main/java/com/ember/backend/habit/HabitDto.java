package com.ember.backend.habit;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HabitDto {
    private Long id;
    private String name;
    private String fullVersion;
    private String liteVersion;
    private String minimalVersion;
    private HabitStatus status;
    private int streakCount;
    private Long userId;
    private LocalDateTime createdAt;
}