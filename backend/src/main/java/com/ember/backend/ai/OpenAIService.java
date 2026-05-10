//package com.ember.backend.ai;
//
//import com.ember.backend.autopsy.AutopsyDto;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * OpenAIService — replaces ClaudeService.java
// *
// * Uses GPT-4.1 Nano via OpenAI Chat Completions API.
// * Config: openai.api.key in application-local.properties (never commit this file)
// *
// * Called by NudgeController for:
// *   - POST /api/users/{id}/ai/nudge          → generateNudge()
// *   - GET  /api/users/{id}/ai/autopsy-insight → generateAutopsyInsight()
// */
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class OpenAIService {
//
//    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
//    private static final String MODEL    = "gpt-4.1-nano";
//
//    @Value("${openai.api.key:}")
//    private String apiKey;
//
//    private final RestTemplate restTemplate;
//    private final ObjectMapper objectMapper;
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // Public API
//    // ─────────────────────────────────────────────────────────────────────────
//
//    /**
//     * Generates a short motivational nudge based on the user's current energy,
//     * sleep, HRV data, and today's habits.
//     *
//     * @param userName     User's first name
//     * @param energyScore  1–5 energy score for today
//     * @param sleepHours   hours slept last night (nullable)
//     * @param hrvMs        HRV in milliseconds (nullable)
//     * @param habitNames   List of today's habit names
//     * @return A 1–2 sentence nudge string, never null
//     */
//    public String generateNudge(
//            String userName,
//            int energyScore,
//            Double sleepHours,
//            Double hrvMs,
//            List<String> habitNames
//    ) {
//        if (!isConfigured()) {
//            log.warn("OpenAI API key not configured — returning fallback nudge");
//            return getFallbackNudge(energyScore);
//        }
//
//        String prompt = buildNudgePrompt(userName, energyScore, sleepHours, hrvMs, habitNames);
//        return callOpenAI(prompt, 150);
//    }
//
//    /**
//     * Generates a weekly autopsy insight from the 7-day pattern summary.
//     *
//     * @param userName   User's first name
//     * @param autopsy    The full AutopsyDto from AutopsyService
//     * @return A short paragraph of insight, never null
//     */
//    public String generateAutopsyInsight(String userName, AutopsyDto autopsy) {
//        if (!isConfigured()) {
//            log.warn("OpenAI API key not configured — returning fallback autopsy insight");
//            return getFallbackAutopsyInsight();
//        }
//
//        String prompt = buildAutopsyPrompt(userName, autopsy);
//        return callOpenAI(prompt, 300);
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // Core HTTP call
//    // ─────────────────────────────────────────────────────────────────────────
//
//    /**
//     * Sends a user message to OpenAI Chat Completions and returns the text reply.
//     *
//     * Request shape:
//     * {
//     *   "model": "gpt-4.1-nano",
//     *   "max_tokens": <maxTokens>,
//     *   "messages": [
//     *     { "role": "system", "content": "<system prompt>" },
//     *     { "role": "user",   "content": "<user prompt>" }
//     *   ]
//     * }
//     *
//     * Response path: choices[0].message.content
//     */
//    private String callOpenAI(String userPrompt, int maxTokens) {
//        try {
//            HttpHeaders headers = buildHeaders();
//            ObjectNode body     = buildRequestBody(userPrompt, maxTokens);
//
//            HttpEntity<String> request = new HttpEntity<>(
//                    objectMapper.writeValueAsString(body), headers
//            );
//
//            ResponseEntity<String> response = restTemplate.exchange(
//                    API_URL, HttpMethod.POST, request, String.class
//            );
//
//            return parseResponse(response.getBody());
//
//        } catch (Exception e) {
//            log.error("OpenAI API call failed: {}", e.getMessage(), e);
//            return "Keep going — small steps still move you forward. 🔥";
//        }
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // Request / response helpers
//    // ─────────────────────────────────────────────────────────────────────────
//
//    private HttpHeaders buildHeaders() {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Authorization", "Bearer " + apiKey);
//        return headers;
//    }
//
//    private ObjectNode buildRequestBody(String userPrompt, int maxTokens) {
//        ObjectNode body = objectMapper.createObjectNode();
//        body.put("model", MODEL);
//        body.put("max_tokens", maxTokens);
//
//        // System message keeps replies short and in Ember's tone
//        ArrayNode messages = objectMapper.createArrayNode();
//
//        ObjectNode systemMsg = objectMapper.createObjectNode();
//        systemMsg.put("role", "system");
//        systemMsg.put("content",
//                "You are Ember, a warm and encouraging habit coach. " +
//                        "Keep responses concise, practical, and energising. " +
//                        "Never use bullet points — write in natural sentences only."
//        );
//        messages.add(systemMsg);
//
//        ObjectNode userMsg = objectMapper.createObjectNode();
//        userMsg.put("role", "user");
//        userMsg.put("content", userPrompt);
//        messages.add(userMsg);
//
//        body.set("messages", messages);
//        return body;
//    }
//
//    /**
//     * Extracts text from: choices[0].message.content
//     */
//    private String parseResponse(String responseBody) throws Exception {
//        JsonNode root    = objectMapper.readTree(responseBody);
//        JsonNode choices = root.path("choices");
//
//        if (choices.isEmpty()) {
//            log.warn("OpenAI returned empty choices array. Body: {}", responseBody);
//            return "You're doing great — keep your streak alive today!";
//        }
//
//        return choices.get(0)
//                .path("message")
//                .path("content")
//                .asText("Keep the momentum going! 🔥");
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // Prompt builders
//    // ─────────────────────────────────────────────────────────────────────────
//
//    private String buildNudgePrompt(String userName, int energyScore, Double sleepHours, Double hrvMs, List<String> habitNames) {
//        StringBuilder sb = new StringBuilder();
//        sb.append("Hi ").append(userName).append("! Your energy score today is ").append(energyScore).append("/5. ");
//
//        if (sleepHours != null) {
//            sb.append("You slept ").append(sleepHours).append(" hours last night. ");
//        }
//        if (hrvMs != null) {
//            sb.append("Your HRV is ").append(hrvMs).append(" ms. ");
//        }
//
//        if (habitNames != null && !habitNames.isEmpty()) {
//            sb.append("Your habits today: ").append(String.join(", ", habitNames)).append(". ");
//        }
//
//        sb.append("Write a 1–2 sentence motivational nudge that acknowledges their current state ");
//        sb.append("and encourages them to complete the habit version suited to their energy. ");
//        sb.append("Be warm, specific, and brief. Sign off with one emoji.");
//
//        return sb.toString();
//    }
//
//    private String buildAutopsyPrompt(String userName, AutopsyDto autopsy) {
//        StringBuilder sb = new StringBuilder();
//        sb.append("Analyze ").append(userName).append("'s weekly autopsy data:\n\n");
//        sb.append("Average Energy: ").append(autopsy.getAvgEnergyScore()).append("/5\n");
//        sb.append("Consistency: ").append(autopsy.getConsistencyScore()).append("%\n");
//        sb.append("Habit Completion: ").append(autopsy.getHabitCompletionRate()).append("%\n");
//        sb.append("Patterns: ").append(String.join("; ", autopsy.getPatterns())).append("\n\n");
//        sb.append("Write a short paragraph (3–4 sentences) of personalised insight. ");
//        sb.append("Highlight one pattern you notice, acknowledge their wins, and suggest one small adjustment ");
//        sb.append("for next week. Be encouraging but honest.");
//
//        return sb.toString();
//    }
//
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // Fallbacks (no API key configured)
//    // ─────────────────────────────────────────────────────────────────────────
//
//    private boolean isConfigured() {
//        return apiKey != null && !apiKey.isBlank();
//    }
//
//    private String getFallbackNudge(int energyScore) {
//        return switch (energyScore) {
//            case 1 -> "Low energy today — even your minimal version counts. Rest is part of the process. 💛";
//            case 2 -> "It's a lite day. Small action is still action — you've got this. ✨";
//            case 3 -> "Solid energy today! Your normal version is well within reach. Keep the streak alive. ⚡";
//            case 4 -> "Good energy — push for your full version if you can. You're building something real. 🔥";
//            case 5 -> "Peak energy! Go all in today — your future self will thank you. 🚀";
//            default -> "Every check-in is a win. Stay consistent and the results will follow. 🔥";
//        };
//    }
//
//    private String getFallbackAutopsyInsight() {
//        return "Your weekly patterns show real consistency — every check-in you complete is a data point "
//                + "that helps Ember understand you better. Connect an AI key in your settings to unlock "
//                + "personalised insights based on your energy trends and habit streaks.";
//    }
//}

package com.ember.backend.ai;

import com.ember.backend.autopsy.AutopsyDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * OpenAIService
 *
 * Generates two types of AI content for Ember:
 *
 * 1. generateNudge()       — Duolingo-style smart notification
 *                            Tells the user WHICH habit version to do,
 *                            in what order, and why — based on energy + habit context.
 *
 * 2. generateAutopsyInsight() — Weekly pattern analysis paragraph
 *
 * Config: openai.api.key in application-local.properties (never commit)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIService {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL   = "gpt-4.1-nano";

    @Value("${openai.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generates a Duolingo-style smart notification.
     *
     * Output example (energy 2/5, habits: Meditation + Reading + Exercise):
     * "Low energy today 😴 Start with Meditation (5 min breath focus) — it's your
     *  minimal version and takes almost no effort. Then try Reading (1 page).
     *  Skip Exercise for now — protect that 7-day streak by doing the lite version
     *  when you feel ready. Small wins still count 🔥"
     *
     * @param energyScore  1–5
     * @param sleepHours   nullable
     * @param hrvMs        nullable
     * @param habits       list of the user's active habits with all 3 versions
     */
    public String generateNudge(int energyScore, Double sleepHours, Integer hrvMs, List<HabitContext> habits) {
        if (!isConfigured()) {
            log.warn("OpenAI API key not configured — returning fallback nudge");
            return getFallbackNudge(energyScore, habits);
        }

        String prompt = buildNudgePrompt(energyScore, sleepHours, hrvMs, habits);
        return callOpenAI(prompt, 250);
    }

    /**
     * Generates a weekly autopsy insight paragraph.
     *
     * @param userName   User's first name
     * @param autopsy    The full AutopsyDto from AutopsyService
     * @return A short paragraph of insight, never null
     */
    public String generateAutopsyInsight(String userName, AutopsyDto autopsy) {
        if (!isConfigured()) {
            log.warn("OpenAI API key not configured — returning fallback autopsy insight");
            return getFallbackAutopsyInsight();
        }

        String prompt = buildAutopsyPrompt(userName, autopsy);
        return callOpenAI(prompt, 300);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Core HTTP call
    // ─────────────────────────────────────────────────────────────────────────

    private String callOpenAI(String userPrompt, int maxTokens) {
        try {
            HttpHeaders headers = buildHeaders();
            ObjectNode body     = buildRequestBody(userPrompt, maxTokens);

            HttpEntity<String> request = new HttpEntity<>(
                    objectMapper.writeValueAsString(body), headers
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    API_URL, HttpMethod.POST, request, String.class
            );

            return parseResponse(response.getBody());

        } catch (Exception e) {
            log.error("OpenAI API call failed: {}", e.getMessage(), e);
            return "Keep going — small steps still move you forward. 🔥";
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Request / response helpers
    // ─────────────────────────────────────────────────────────────────────────

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        return headers;
    }

    private ObjectNode buildRequestBody(String userPrompt, int maxTokens) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", MODEL);
        body.put("max_tokens", maxTokens);

        ArrayNode messages = objectMapper.createArrayNode();

        ObjectNode systemMsg = objectMapper.createObjectNode();
        systemMsg.put("role", "system");
        systemMsg.put("content",
                "You are Ember, a smart habit coach inside a mobile app. " +
                        "Your job is to send Duolingo-style notifications — short, punchy, and actionable. " +
                        "Always tell the user WHICH habit version to do (minimal/lite/full) based on their energy, " +
                        "suggest a natural order to do them in, and mention streaks when they matter. " +
                        "Write in 3–5 sentences max. No bullet points. Use 1–2 emojis naturally. " +
                        "Be warm and direct — like a coach, not just a cheerleader."
        );
        messages.add(systemMsg);

        ObjectNode userMsg = objectMapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        messages.add(userMsg);

        body.set("messages", messages);
        return body;
    }

    private String parseResponse(String responseBody) throws Exception {
        JsonNode root    = objectMapper.readTree(responseBody);
        JsonNode choices = root.path("choices");

        if (choices.isEmpty()) {
            log.warn("OpenAI returned empty choices. Body: {}", responseBody);
            return "You're doing great — keep your streak alive today!";
        }

        return choices.get(0)
                .path("message")
                .path("content")
                .asText("Keep the momentum going! 🔥");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Prompt builders
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds the nudge prompt with full habit context.
     *
     * Example output sent to OpenAI:
     *
     * "Energy score: 2/5. Sleep: 5.5 hours. HRV: 42ms.
     *  The user has 3 active habits today:
     *  1. Meditation — minimal: '2 min breathing', lite: '10 min guided', full: '20 min deep session' — streak: 5 days
     *  2. Reading — minimal: '1 page', lite: '10 pages', full: '30 pages' — streak: 12 days
     *  3. Exercise — minimal: '5 min walk', lite: '20 min jog', full: '45 min workout' — streak: 2 days
     *
     *  Based on this energy level, tell the user which version of each habit to do today,
     *  suggest a good order to do them, and mention any streaks worth protecting."
     */
    private String buildNudgePrompt(int energyScore, Double sleepHours, Integer hrvMs, List<HabitContext> habits) {
        StringBuilder sb = new StringBuilder();

        // Energy context
        sb.append("Energy score: ").append(energyScore).append("/5. ");
        if (sleepHours != null) sb.append("Sleep: ").append(sleepHours).append(" hours. ");
        if (hrvMs != null)      sb.append("HRV: ").append(hrvMs).append("ms. ");
        sb.append("\n\n");

        // Habit list with all 3 versions
        if (habits == null || habits.isEmpty()) {
            sb.append("The user has no active habits set up yet.");
        } else {
            sb.append("The user has ").append(habits.size()).append(" active habit(s) today:\n");
            for (int i = 0; i < habits.size(); i++) {
                HabitContext h = habits.get(i);
                sb.append(i + 1).append(". ").append(h.getName())
                        .append(" — minimal: '").append(h.getMinimalVersion()).append("'")
                        .append(", lite: '").append(h.getLiteVersion()).append("'")
                        .append(", full: '").append(h.getFullVersion()).append("'")
                        .append(" — streak: ").append(h.getStreakCount()).append(" day(s)\n");
            }
        }

        sb.append("\nBased on this energy level, write a notification that tells the user: ");
        sb.append("(1) which version of each habit to do today and why, ");
        sb.append("(2) a suggested order to tackle them, ");
        sb.append("(3) mention any streaks worth protecting. ");
        sb.append("Keep it under 5 sentences. Sound like a smart coach, not a motivational poster.");

        return sb.toString();
    }

    private String buildAutopsyPrompt(String userName, AutopsyDto autopsy) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analyze ").append(userName).append("'s weekly autopsy data:\n\n");
        sb.append("Average Energy: ").append(autopsy.getAvgEnergyScore()).append("/5\n");
        sb.append("Total Check-ins: ").append(autopsy.getTotalCheckIns()).append("/7\n");
        sb.append("Consistency Score: ").append(autopsy.getConsistencyScore()).append("%\n");
        sb.append("Habit Completion: ").append(autopsy.getHabitCompletionRate()).append("%\n");
        
        if (autopsy.getPatterns() != null && !autopsy.getPatterns().isEmpty()) {
            sb.append("Detected Patterns: \n");
            for (String pattern : autopsy.getPatterns()) {
                sb.append("  - ").append(pattern).append("\n");
            }
        }
        
        sb.append("\nWrite a short paragraph (3–4 sentences) of personalised insight. ");
        sb.append("Highlight one pattern you notice, acknowledge their wins, and suggest one small adjustment ");
        sb.append("for next week. Be encouraging but honest.");

        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fallbacks (no API key configured)
    // ─────────────────────────────────────────────────────────────────────────

    private boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Fallback nudge that still uses habit data even without an API key.
     * Picks the right version based on energy score so it's not completely generic.
     */
    private String getFallbackNudge(int energyScore, List<HabitContext> habits) {
        String version = switch (energyScore) {
            case 1, 2 -> "minimal";
            case 3    -> "lite";
            default   -> "full";
        };

        String emoji = switch (energyScore) {
            case 1, 2 -> "😴";
            case 3    -> "⚡";
            default   -> "🔥";
        };

        if (habits == null || habits.isEmpty()) {
            return "Energy at " + energyScore + "/5 today " + emoji +
                    " — set up your first habit to get personalised guidance!";
        }

        // Find the habit with the highest streak to highlight
        HabitContext topStreak = habits.stream()
                .max((a, b) -> Integer.compare(a.getStreakCount(), b.getStreakCount()))
                .orElse(habits.get(0));

        String versionText = switch (version) {
            case "minimal" -> topStreak.getMinimalVersion();
            case "full"    -> topStreak.getFullVersion();
            default        -> topStreak.getLiteVersion();
        };

        return "Energy at " + energyScore + "/5 today " + emoji +
                " — go with your " + version + " versions. Start with " +
                topStreak.getName() + ": \"" + versionText + "\". " +
                (topStreak.getStreakCount() > 0
                        ? "You have a " + topStreak.getStreakCount() + "-day streak to protect — don't break it now. 🔥"
                        : "Small action today builds the foundation for tomorrow.");
    }

    private String getFallbackAutopsyInsight() {
        return "Your weekly patterns show real consistency — every check-in you complete is a data point " +
                "that helps Ember understand you better. Connect an OpenAI key in your config to unlock " +
                "personalised insights based on your energy trends and habit streaks.";
    }
}
