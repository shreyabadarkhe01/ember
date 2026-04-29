package com.ember.backend.biometric;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/biometric-checkin")
@RequiredArgsConstructor
@Tag(name = "Biometric Check-in", description = "Auto check-in using Samsung Health / Health Connect biometric data")
public class BiometricCheckinController {

    private final BiometricCheckinService biometricCheckinService;

    /**
     * POST /api/users/{userId}/biometric-checkin
     *
     * Called by:
     * - Flutter app after reading Samsung Health data automatically
     * - Web app when Health Connect data is available
     *
     * Request body example (from Samsung Health via Flutter):
     * {
     *   "sleepHours": 7.5,
     *   "hrvMs": 55.0,
     *   "restingHeartRate": 62,
     *   "steps": 9200,
     *   "caloriesBurned": 2300,
     *   "source": "samsung_health",
     *   "note": "Felt great this morning"
     * }
     *
     * Response:
     * {
     *   "checkInId": 42,
     *   "energyScore": 4,
     *   "breakdown": {
     *     "sleepHours": 7.5, "sleepScore": 2.0,
     *     "hrvMs": 55.0,     "hrvScore": 1.5,
     *     "restingHeartRate": 62, "rhrScore": 1.0,
     *     "totalScore": 4,
     *     "summary": "Good energy levels 😊 — full habits on track"
     *   },
     *   "source": "samsung_health"
     * }
     */
    @PostMapping
    @Operation(summary = "Auto check-in from biometric data",
               description = "Calculates energy score from Samsung Health biometrics and creates a check-in")
    public ResponseEntity<BiometricCheckinResponseDto> biometricCheckin(
            @PathVariable Long userId,
            @Valid @RequestBody BiometricDataDto dto
    ) {
        return ResponseEntity.ok(biometricCheckinService.processCheckin(userId, dto));
    }

    /**
     * POST /api/users/{userId}/biometric-checkin/preview
     *
     * Calculates energy score WITHOUT saving a check-in.
     * Used by frontend to show user "your score would be X" before confirming.
     */
    @PostMapping("/preview")
    @Operation(summary = "Preview energy score without saving",
               description = "Calculates and returns energy score from biometrics without creating a check-in")
    public ResponseEntity<BiometricCheckinResponseDto> previewScore(
            @PathVariable Long userId,
            @Valid @RequestBody BiometricDataDto dto
    ) {
        // Calculate only, don't save
        var response = biometricCheckinService.previewOnly(userId, dto);
        return ResponseEntity.ok(response);
    }
}
