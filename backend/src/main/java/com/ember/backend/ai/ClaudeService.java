package com.ember.backend.ai;

import com.ember.backend.ai.ClaudeApiDtos.ClaudeRequest;
import com.ember.backend.ai.ClaudeApiDtos.ClaudeResponse;
import com.ember.backend.ai.ClaudeApiDtos.Message;
import com.ember.backend.autopsy.AutopsyDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Service for all Claude AI interactions in Ember.
 *
 * Three main capabilities:
 * 1. generateNudge()        — motivational message after daily check-in
 * 2. generateAutopsyInsight() — weekly pattern analysis and recommendations
 * 3. suggestHabitAdjustment() — suggests if habit targets need changing
 *
 * API key stored in application-local.properties (never commit this file):
 *   claude.api.key=sk-ant-xxxxx
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeService {

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-4-20250514";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    @Value("${claude.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // ── System prompt — defines Claude's persona for Ember ───────
    private static final String SYSTEM_PROMPT = """
            You are Ember's wellness coach — warm, concise, and data-driven.
            You help users build adaptive habits based on their daily energy levels and biometric data.
            
            Your tone is:
            - Encouraging but honest
            - Short and actionable (never more than 3 sentences unless asked)
            - Science-informed but not clinical
            - Warm, like a supportive friend who knows health data
            
            Never use generic motivational quotes.
            Always ground your response in the specific data provided.
            Use emojis sparingly — 1 max per response.
            """;

    // ════════════════════════════════════════════════════════════
    // METHOD 1 — Daily Nudge
    // Called after every check-in to give user a personalised message
    // ════════════════════════════════════════════════════════════

    /**
     * Generate a short motivational nudge after a check-in.
     *
     * @param userName      User's first name
     * @param energyScore   Today's energy score (1-5)
     * @param sleepHours    Sleep hours if available (nullable)
     * @param hrvMs         HRV in ms if available (nullable)
     * @param habitNames    List of today's habit names
     * @return Short nudge message (1-2 sentences)
     */
    public String generateNudge(
            String userName,
            int energyScore,
            Double sleepHours,
            Double hrvMs,
            List<String> habitNames
    ) {
        String habitsText = habitNames.isEmpty() ? "no habits set yet" :
                String.join(", ", habitNames);

        String sleepText = sleepHours != null ?
                String.format("Sleep last night: %.1f hours.", sleepHours) : "";
        String hrvText = hrvMs != null ?
                String.format("HRV: %.0f ms.", hrvMs) : "";

        String prompt = String.format("""
                User: %s
                Today's energy score: %d/5
                %s
                %s
                Today's habits: %s
                
                Write a personalised nudge message for %s based on their energy level today.
                Maximum 2 sentences. Be specific to their score and habits.
                """,
                userName, energyScore, sleepText, hrvText, habitsText, userName);

        return callClaude(prompt, 150);
    }

    // ════════════════════════════════════════════════════════════
    // METHOD 2 — Weekly Autopsy Insight
    // Called when user views their weekly autopsy report
    // ════════════════════════════════════════════════════════════

    /**
     * Generate a weekly insight based on autopsy data.
     *
     * @param userName   User's first name
     * @param autopsy    The full AutopsyDto from AutopsyService
     * @return Insight paragraph with actionable recommendation (3-5 sentences)
     */
    public String generateAutopsyInsight(String userName, AutopsyDto autopsy) {

        String patternsText = autopsy.getPatterns() != null ?
                String.join("; ", autopsy.getPatterns()) : "none detected";

        String prompt = String.format("""
                Weekly autopsy report for %s:
                - Average energy score: %.1f/5
                - Best day: %s | Worst day: %s
                - High energy days: %d | Low energy days: %d
                - Consistency: %d%% (%d/7 days checked in)
                - Habit completion rate: %d%%
                - Week summary: %s
                - Detected patterns: %s
                - Sleep correlation: %s
                - HRV correlation: %s
                
                Write a personalised weekly insight for %s.
                Include: what went well, what to watch out for, and one specific actionable recommendation.
                Maximum 4 sentences. Ground every point in their actual data.
                """,
                userName,
                autopsy.getAvgEnergyScore(),
                autopsy.getBestDay(), autopsy.getWorstDay(),
                autopsy.getHighEnergyDays(), autopsy.getLowEnergyDays(),
                autopsy.getConsistencyScore(), autopsy.getTotalCheckIns(),
                autopsy.getHabitCompletionRate(),
                autopsy.getWeekSummary(),
                patternsText,
                autopsy.getSleepCorrelation(),
                autopsy.getHrvCorrelation(),
                userName);

        return callClaude(prompt, 300);
    }

    // ════════════════════════════════════════════════════════════
    // METHOD 3 — Habit Adjustment Suggestion
    // Called from autopsy if a habit is consistently missed/exceeded
    // ════════════════════════════════════════════════════════════

    /**
     * Suggest if a habit's targets should be permanently adjusted.
     *
     * @param userName          User's first name
     * @param habitName         Name of the habit e.g. "Morning run"
     * @param unit              Unit e.g. "minutes"
     * @param lightTarget       Current light target
     * @param standardTarget    Current standard target
     * @param challengeTarget   Current challenge target
     * @param completionRate    How often this habit was completed this week (%)
     * @param avgEnergyOnMiss   Average energy score on days habit was missed
     * @return Suggestion string (1-2 sentences)
     */
    public String suggestHabitAdjustment(
            String userName,
            String habitName,
            String unit,
            int lightTarget,
            int standardTarget,
            int challengeTarget,
            int completionRate,
            double avgEnergyOnMiss
    ) {
        String prompt = String.format("""
                User: %s
                Habit: %s
                Current targets: Light=%d %s | Standard=%d %s | Challenge=%d %s
                This week's completion rate: %d%%
                Average energy score on days this habit was missed: %.1f/5
                
                Should %s adjust the targets for "%s"?
                Give one specific suggestion — either keep targets, scale them down, or scale them up.
                Maximum 2 sentences.
                """,
                userName,
                habitName,
                lightTarget, unit, standardTarget, unit, challengeTarget, unit,
                completionRate,
                avgEnergyOnMiss,
                userName, habitName);

        return callClaude(prompt, 150);
    }

    // ════════════════════════════════════════════════════════════
    // Core API caller — used by all 3 methods above
    // ════════════════════════════════════════════════════════════

    private String callClaude(String userPrompt, int maxTokens) {
        try {
            ClaudeRequest request = ClaudeRequest.builder()
                    .model(MODEL)
                    .maxTokens(maxTokens)
                    .systemPrompt(SYSTEM_PROMPT)
                    .messages(List.of(
                            Message.builder()
                                    .role("user")
                                    .content(userPrompt)
                                    .build()
                    ))
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", ANTHROPIC_VERSION);

            HttpEntity<ClaudeRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ClaudeResponse> response = restTemplate.exchange(
                    CLAUDE_API_URL,
                    HttpMethod.POST,
                    entity,
                    ClaudeResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String result = response.getBody().getTextContent();
                log.info("Claude response received — {} tokens used",
                        response.getBody().getUsage() != null ?
                                response.getBody().getUsage().getOutputTokens() : "unknown");
                return result;
            }

            log.warn("Claude API returned non-2xx status: {}", response.getStatusCode());
            return getFallbackMessage(userPrompt);

        } catch (Exception e) {
            log.error("Claude API call failed: {}", e.getMessage());
            return getFallbackMessage(userPrompt);
        }
    }

    /**
     * Fallback message if Claude API is unavailable.
     * App should still work even if AI is down.
     */
    private String getFallbackMessage(String prompt) {
        if (prompt.contains("nudge") || prompt.contains("energy score")) {
            return "Keep going — every check-in builds your self-awareness 💪";
        }
        if (prompt.contains("autopsy") || prompt.contains("weekly")) {
            return "Your week has been tracked. Keep checking in daily for deeper insights next week.";
        }
        return "Stay consistent — small daily actions compound into big results.";
    }
}
