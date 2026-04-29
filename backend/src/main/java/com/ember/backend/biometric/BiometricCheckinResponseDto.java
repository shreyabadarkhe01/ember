package com.ember.backend.biometric;

import com.ember.backend.energy.EnergyBreakdown;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class BiometricCheckinResponseDto {

    private Long checkInId;
    private Long userId;
    private Integer energyScore;       // 1-5
    private EnergyBreakdown breakdown; // why this score
    private LocalDate date;
    private String source;             // "samsung_health", "manual", "health_connect"
}
