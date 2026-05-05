package com.ember.backend.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTOs for Anthropic Claude API communication.
 * API docs: https://docs.anthropic.com/en/api/messages
 */
public class ClaudeApiDtos {

    // ── Request ──────────────────────────────────────────────────
    @Data
    @Builder
    public static class ClaudeRequest {
        private String model;

        @JsonProperty("max_tokens")
        private Integer maxTokens;

        private List<Message> messages;

        @JsonProperty("system")
        private String systemPrompt;
    }

    @Data
    @Builder
    public static class Message {
        private String role;    // "user" or "assistant"
        private String content;
    }

    // ── Response ─────────────────────────────────────────────────
    @Data
    public static class ClaudeResponse {
        private String id;
        private List<ContentBlock> content;
        private Usage usage;

        public String getTextContent() {
            if (content == null || content.isEmpty()) return "";
            return content.stream()
                    .filter(c -> "text".equals(c.getType()))
                    .map(ContentBlock::getText)
                    .findFirst()
                    .orElse("");
        }
    }

    @Data
    public static class ContentBlock {
        private String type;
        private String text;
    }

    @Data
    public static class Usage {
        @JsonProperty("input_tokens")
        private Integer inputTokens;

        @JsonProperty("output_tokens")
        private Integer outputTokens;
    }
}
