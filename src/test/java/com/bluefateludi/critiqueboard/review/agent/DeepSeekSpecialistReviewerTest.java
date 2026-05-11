package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.ai.DeepSeekProperties;
import com.bluefateludi.critiqueboard.review.domain.AgentRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeepSeekSpecialistReviewerTest {

    @Test
    void parsesStructuredJsonResponseIntoCritiqueResult() {
        CapturingChatModel chatModel = new CapturingChatModel("""
                {
                  "score": 88,
                  "feedback": "The structure is clear and easy to follow.",
                  "suggestions": ["Move the conclusion earlier."],
                  "confidence": 0.91,
                  "evidence": [
                    {
                      "documentChunkId": "00000000-0000-0000-0000-000000000001",
                      "quote": "launch plan",
                      "reason": "This quote anchors the structure feedback."
                    }
                  ]
                }
                """);
        SpecialistReviewer fallback = new DeterministicSpecialistReviewer();
        DeepSeekSpecialistReviewer reviewer = new DeepSeekSpecialistReviewer(
                chatModel,
                new ObjectMapper(),
                enabledProperties(),
                fallback
        );

        CritiqueResult result = reviewer.review(new SpecialistReviewRequest(
                UUID.randomUUID(),
                AgentRole.STRUCTURE,
                1,
                "Review document structure."
        ));

        assertThat(chatModel.lastPrompt).contains("STRUCTURE");
        assertThat(result.role()).isEqualTo(AgentRole.STRUCTURE);
        assertThat(result.score()).isEqualTo(88);
        assertThat(result.feedback()).contains("structure is clear");
        assertThat(result.suggestions()).containsExactly("Move the conclusion earlier.");
        assertThat(result.confidence()).isEqualTo(0.91);
        assertThat(result.evidence()).hasSize(1);
    }

    @Test
    void fallsBackWhenModelCallFails() {
        ChatModel failingModel = new ChatModel() {
            @Override
            public String chat(String message) {
                throw new IllegalStateException("LLM unavailable");
            }
        };
        DeepSeekSpecialistReviewer reviewer = new DeepSeekSpecialistReviewer(
                failingModel,
                new ObjectMapper(),
                enabledProperties(),
                new DeterministicSpecialistReviewer()
        );

        CritiqueResult result = reviewer.review(new SpecialistReviewRequest(
                UUID.randomUUID(),
                AgentRole.RISK,
                1,
                "Review risks."
        ));

        assertThat(result.role()).isEqualTo(AgentRole.RISK);
        assertThat(result.feedback()).containsIgnoringCase("risk");
    }

    @Test
    void fallsBackWithoutCallingModelWhenApiKeyIsBlank() {
        CapturingChatModel chatModel = new CapturingChatModel("{}");
        DeepSeekSpecialistReviewer reviewer = new DeepSeekSpecialistReviewer(
                chatModel,
                new ObjectMapper(),
                new DeepSeekProperties(true, "https://api.deepseek.com", "", "deepseek-chat", 60),
                new DeterministicSpecialistReviewer()
        );

        CritiqueResult result = reviewer.review(new SpecialistReviewRequest(
                UUID.randomUUID(),
                AgentRole.LOGIC,
                1,
                "Review logic."
        ));

        assertThat(chatModel.lastPrompt).isNull();
        assertThat(result.role()).isEqualTo(AgentRole.LOGIC);
        assertThat(result.feedback()).containsIgnoringCase("reasoning");
    }

    private DeepSeekProperties enabledProperties() {
        return new DeepSeekProperties(true, "https://api.deepseek.com", "test-key", "deepseek-chat", 60);
    }

    private static class CapturingChatModel implements ChatModel {
        private final String response;
        private String lastPrompt;

        private CapturingChatModel(String response) {
            this.response = response;
        }

        @Override
        public String chat(String message) {
            this.lastPrompt = message;
            return response;
        }
    }
}
