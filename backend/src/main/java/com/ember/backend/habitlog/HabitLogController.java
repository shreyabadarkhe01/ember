// HabitLogController.java
package com.ember.backend.habitlog;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/habit-logs")
@RequiredArgsConstructor
public class HabitLogController {

    private final HabitLogRepository habitLogRepository;

    // GET /api/users/{userId}/habit-logs?from=2026-05-01&to=2026-05-07
    @GetMapping
    public ResponseEntity<List<HabitLog>> getLogs(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(
                habitLogRepository.findByUserIdAndDateBetween(userId, from, to)
        );
    }
}