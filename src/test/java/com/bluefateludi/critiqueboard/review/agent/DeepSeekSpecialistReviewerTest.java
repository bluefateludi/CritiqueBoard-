package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.ai.DeepSeekProperties;
import com.bluefateludi.critiqueboard.review.domain.AgentRole;
import com.bluefateludi.critiqueboard.review.service.TokenUsageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
                fallback,
                mock(TokenUsageService.class)
        );

        CritiqueResult result = reviewer.review(new SpecialistReviewRequest(
                UUID.randomUUID(),
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
                new DeterministicSpecialistReviewer(),
                mock(TokenUsageService.class)
        );

        CritiqueResult result = reviewer.review(new SpecialistReviewRequest(
                UUID.randomUUID(),
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
                new DeepSeekProperties(
                        true,
                        "https://api.deepseek.com",
                        "",
                        "deepseek-chat",
                        60,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ),
                new DeterministicSpecialistReviewer(),
                mock(TokenUsageService.class)
        );

        CritiqueResult result = reviewer.review(new SpecialistReviewRequest(
                UUID.randomUUID(),
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
        return new DeepSeekProperties(
                true,
                "https://api.deepseek.com",
                "test-key",
                "deepseek-chat",
                60,
                new BigDecimal("0.14000000"),
                new BigDecimal("0.28000000")
        );
    }

    @Test
    void recordsTokenUsageWhenModelReturnsUsageMetadata() {
        UUID reviewTaskId = UUID.randomUUID();
        UUID agentRunId = UUID.randomUUID();
        CapturingChatModel chatModel = new CapturingChatModel("""
                {
                  "score": 88,
                  "feedback": "The structure is clear.",
                  "suggestions": [],
                  "confidence": 0.91,
                  "evidence": []
                }
                """, new TokenUsage(100, 50));
        TokenUsageService tokenUsageService = mock(TokenUsageService.class);
        DeepSeekSpecialistReviewer reviewer = new DeepSeekSpecialistReviewer(
                chatModel,
                new ObjectMapper(),
                enabledProperties(),
                new DeterministicSpecialistReviewer(),
                tokenUsageService
        );

        reviewer.review(new SpecialistReviewRequest(reviewTaskId, agentRunId, AgentRole.STRUCTURE, 1, "Review structure."));

        verify(tokenUsageService).recordModelUsage(
                eq(reviewTaskId),
                eq(agentRunId),
                eq("deepseek-chat"),
                eq(chatModel.tokenUsage),
                eq(new BigDecimal("0.14000000")),
                eq(new BigDecimal("0.28000000"))
        );
    }

    private static class CapturingChatModel implements ChatModel {
        private final String response;
        private final TokenUsage tokenUsage;
        private String lastPrompt;

        private CapturingChatModel(String response) {
            this(response, null);
        }

        private CapturingChatModel(String response, TokenUsage tokenUsage) {
            this.response = response;
            this.tokenUsage = tokenUsage;
        }

        @Override
        public String chat(String message) {
            this.lastPrompt = message;
            return response;
        }

        @Override
        public ChatResponse chat(ChatRequest request) {
            this.lastPrompt = request.messages().getFirst().toString();
            return ChatResponse.builder()
                    .aiMessage(AiMessage.from(response))
                    .modelName("deepseek-chat")
                    .tokenUsage(tokenUsage)
                    .build();
        }
    }
}
