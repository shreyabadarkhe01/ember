
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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
