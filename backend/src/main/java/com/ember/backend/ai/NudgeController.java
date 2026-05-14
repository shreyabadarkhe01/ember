//package com.ember.backend.ai;
//
//import com.ember.backend.autopsy.AutopsyDto;
//import com.ember.backend.autopsy.AutopsyService;
//import com.ember.backend.common.AppException;
//import com.ember.backend.habit.Habit;
//import com.ember.backend.habit.HabitRepository;
//import com.ember.backend.user.User;
//import com.ember.backend.user.UserRepository;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/users/{userId}/ai")
//@RequiredArgsConstructor
//@Tag(name = "AI Insights", description = "Claude AI powered nudges and insights")
//public class NudgeController {
//
//    private final OpenAIService openAIService;
//    private final AutopsyService autopsyService;
//    private final UserRepository userRepository;
//    private final HabitRepository habitRepository;
//
//    /**
//     * POST /api/users/{userId}/ai/nudge
//     *
//     * Called after check-in to get a personalised nudge.
//     *
//     * Request body:
//     * {
//     *   "energyScore": 3,
//     *   "sleepHours": 6.5,
//     *   "hrvMs": 42.0
//     * }
//     *
//     * Response:
//     * { "nudge": "Moderate energy today — pace yourself and focus on your standard targets 😊" }
//     */
//    @PostMapping("/nudge")
//    @Operation(summary = "Get AI nudge after check-in")
//    public ResponseEntity<Map<String, String>> getNudge(
//            @PathVariable Long userId,
//            @RequestBody Map<String, Object> body
//    ) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
//
//        int energyScore = (Integer) body.getOrDefault("energyScore", 3);
//        Double sleepHours = body.get("sleepHours") != null ?
//                ((Number) body.get("sleepHours")).doubleValue() : null;
//        Double hrvMs = body.get("hrvMs") != null ?
//                ((Number) body.get("hrvMs")).doubleValue() : null;
//
//        List<String> habitNames = habitRepository.findByUserId(userId)
//                .stream()
//                .map(Habit::getName)
//                .collect(Collectors.toList());
//
//        String firstName = user.getName().split(" ")[0];
//        String nudge = openAIService.generateNudge(
//                firstName, energyScore, sleepHours, hrvMs, habitNames);
//
//        return ResponseEntity.ok(Map.of("nudge", nudge));
//    }
//
//    /**
//     * GET /api/users/{userId}/ai/autopsy-insight
//     *
//     * Returns Claude's analysis of the user's weekly autopsy.
//     * Also enriches the autopsy report with aiInsight field.
//     *
//     * Response:
//     * { "insight": "Claude's weekly insight here..." }
//     */
//    @GetMapping("/autopsy-insight")
//    @Operation(summary = "Get AI insight for weekly autopsy")
//    public ResponseEntity<Map<String, String>> getAutopsyInsight(
//            @PathVariable Long userId
//    ) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
//
//        AutopsyDto autopsy = autopsyService.generateWeeklyAutopsy(userId);
//        String firstName = user.getName().split(" ")[0];
//        String insight = openAIService.generateAutopsyInsight(firstName, autopsy);
//
//        return ResponseEntity.ok(Map.of("insight", insight));
//    }
//}


package com.ember.backend.ai;

import com.ember.backend.autopsy.AutopsyDto;
import com.ember.backend.autopsy.AutopsyService;
import com.ember.backend.checkin.CheckInService;
import com.ember.backend.common.AppException;
import com.ember.backend.habit.Habit;
import com.ember.backend.habit.HabitService;
import com.ember.backend.user.User;
import com.ember.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * NudgeController
 *
 * POST /api/users/{id}/ai/nudge
 *   → Fetches user's active habits from DB
 *   → Passes them + energy data to OpenAIService
 *   → Returns a Duolingo-style smart notification string
 *
 * GET /api/users/{id}/ai/autopsy-insight
 *   → Returns AI analysis of the user's 7-day autopsy summary
 */
@RestController
@RequestMapping("/api/users/{id}/ai")
@RequiredArgsConstructor
public class NudgeController {

    private final OpenAIService openAIService;
    private final HabitService habitService;
    private final AutopsyService autopsyService;
    private final UserRepository userRepository;
    private final CheckInService checkInService;

    /**
     * POST /api/users/{id}/ai/nudge
     *
     * Request body: { energyScore, sleepHours?, hrvMs? }
     *
     * The controller fetches active habits from the DB so the frontend
     * doesn't need to send them — keeps the request payload minimal.
     */
    @PostMapping("/nudge")
    public ResponseEntity<Map<String, String>> getNudge(
            @PathVariable Long id,
            @RequestBody NudgeRequest request
    ) {
        // Fetch user's active habits and map to lightweight context objects
        List<HabitContext> habits = habitService.getUserHabits(id)
                .stream()
                .filter(h -> h.getStatus().name().equals("ACTIVE"))
                .map(h -> new HabitContext(
                        h.getName(),
                        h.getMinimalVersion(),
                        h.getLiteVersion(),
                        h.getFullVersion(),
                        h.getStreakCount()
                ))
                .collect(Collectors.toList());

        String nudge = openAIService.generateNudge(
                request.getEnergyScore(),
                request.getSleepHours(),
                request.getHrvMs(),
                habits
        );
        checkInService.saveNudge(id, nudge);
        return ResponseEntity.ok(Map.of("nudge", nudge));
    }

    /**
     * GET /api/users/{id}/ai/autopsy-insight
     *
     * Returns a paragraph of AI insight from the 7-day autopsy summary.
     * AutopsyService is called here and its summary string is passed to OpenAI.
     */
    @GetMapping("/autopsy-insight")
    public ResponseEntity<Map<String, String>> getAutopsyInsight(@PathVariable Long id) {
        // Get user for name
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        // Generate autopsy data
        AutopsyDto autopsy = autopsyService.generateWeeklyAutopsy(id);
        String userName = user.getName().split(" ")[0];

        // Get AI insight
        String insight = openAIService.generateAutopsyInsight(userName, autopsy);

        return ResponseEntity.ok(Map.of("insight", insight));
    }
}
