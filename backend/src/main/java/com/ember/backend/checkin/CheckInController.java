package com.ember.backend.checkin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping("/{userId}/checkins")
    public ResponseEntity<CheckInDto> createCheckIn(
            @PathVariable Long userId,
            @Valid @RequestBody CheckIn checkIn) {
        return ResponseEntity.ok(checkInService.createCheckIn(userId, checkIn));
    }

    @GetMapping("/{userId}/checkins")
    public ResponseEntity<List<CheckInDto>> getUserCheckIns(
            @PathVariable Long userId) {
        return ResponseEntity.ok(checkInService.getUserCheckIns(userId));
    }

    @GetMapping("/{userId}/checkins/today")
    public ResponseEntity<CheckInDto> getTodayCheckIn(
            @PathVariable Long userId) {
        return ResponseEntity.ok(checkInService.getTodayCheckIn(userId));
    }
}