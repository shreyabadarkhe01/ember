package com.ember.backend.ai;

import com.ember.backend.autopsy.AutopsyDto;
import com.ember.backend.autopsy.AutopsyService;
import com.ember.backend.common.AppException;
import com.ember.backend.habit.Habit;
import com.ember.backend.habit.HabitRepository;
import com.ember.backend.user.User;
import com.ember.backend.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/{userId}/ai")
@RequiredArgsConstructor
@Tag(name = "AI Insights", description = "Claude AI powered nudges and insights")
public class NudgeController {

    private final ClaudeService claudeService;
    private final AutopsyService autopsyService;
    private final UserRepository userRepository;
    private final HabitRepository habitRepository;

    /**
     * POST /api/users/{userId}/ai/nudge
     *
     * Called after check-in to get a personalised nudge.
     *
     * Request body:
     * {
     *   "energyScore": 3,
     *   "sleepHours": 6.5,
     *   "hrvMs": 42.0
     * }
     *
     * Response:
     * { "nudge": "Moderate energy today — pace yourself and focus on your standard targets 😊" }
     */
    @PostMapping("/nudge")
    @Operation(summary = "Get AI nudge after check-in")
    public ResponseEntity<Map<String, String>> getNudge(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> body
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        int energyScore = (Integer) body.getOrDefault("energyScore", 3);
        Double sleepHours = body.get("sleepHours") != null ?
                ((Number) body.get("sleepHours")).doubleValue() : null;
        Double hrvMs = body.get("hrvMs") != null ?
                ((Number) body.get("hrvMs")).doubleValue() : null;

        List<String> habitNames = habitRepository.findByUserId(userId)
                .stream()
                .map(Habit::getName)
                .collect(Collectors.toList());

        String firstName = user.getName().split(" ")[0];
        String nudge = claudeService.generateNudge(
                firstName, energyScore, sleepHours, hrvMs, habitNames);

        return ResponseEntity.ok(Map.of("nudge", nudge));
    }

    /**
     * GET /api/users/{userId}/ai/autopsy-insight
     *
     * Returns Claude's analysis of the user's weekly autopsy.
     * Also enriches the autopsy report with aiInsight field.
     *
     * Response:
     * { "insight": "Claude's weekly insight here..." }
     */
    @GetMapping("/autopsy-insight")
    @Operation(summary = "Get AI insight for weekly autopsy")
    public ResponseEntity<Map<String, String>> getAutopsyInsight(
            @PathVariable Long userId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        AutopsyDto autopsy = autopsyService.generateWeeklyAutopsy(userId);
        String firstName = user.getName().split(" ")[0];
        String insight = claudeService.generateAutopsyInsight(firstName, autopsy);

        return ResponseEntity.ok(Map.of("insight", insight));
    }
}
