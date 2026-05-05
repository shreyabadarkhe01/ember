package com.ember.backend.autopsy;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/autopsy")
@RequiredArgsConstructor
@Tag(name = "Autopsy", description = "Weekly pattern analysis and insights")
public class AutopsyController {

    private final AutopsyService autopsyService;

    /**
     * GET /api/users/{userId}/autopsy
     *
     * Returns a full weekly autopsy report including:
     * - Average energy score
     * - Best and worst days
     * - Habit completion rate
     * - Detected patterns
     * - AI insight (from Claude)
     * - Daily energy breakdown for chart
     */
    @GetMapping
    @Operation(
        summary = "Get weekly autopsy report",
        description = "Analyses last 7 days of check-ins and habits, returns patterns and AI insight"
    )
    public ResponseEntity<AutopsyDto> getWeeklyAutopsy(@PathVariable Long userId) {
        return ResponseEntity.ok(autopsyService.generateWeeklyAutopsy(userId));
    }
}
