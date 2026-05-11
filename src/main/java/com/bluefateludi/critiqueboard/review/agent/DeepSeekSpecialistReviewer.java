package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.ai.DeepSeekProperties;
import com.bluefateludi.critiqueboard.review.domain.AgentRole;
import com.bluefateludi.critiqueboard.review.service.TokenUsageService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Primary
public class DeepSeekSpecialistReviewer implements SpecialistReviewer {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final DeepSeekProperties properties;
    private final SpecialistReviewer fallback;
    private final TokenUsageService tokenUsageService;

    public DeepSeekSpecialistReviewer(
            ChatModel chatModel,
            ObjectMapper objectMapper,
            DeepSeekProperties properties,
            @Qualifier("deterministicSpecialistReviewer") SpecialistReviewer fallback,
            TokenUsageService tokenUsageService
    ) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.fallback = fallback;
        this.tokenUsageService = tokenUsageService;
    }

    @Override
    public CritiqueResult review(SpecialistReviewRequest request) {
        if (!properties.enabled() || properties.apiKey() == null || properties.apiKey().isBlank()) {
            return fallback.review(request);
        }
        try {
            ChatResponse response = chatModel.chat(ChatRequest.builder()
                    .messages(UserMessage.from(promptFor(request)))
                    .build());
            recordTokenUsage(request, response.tokenUsage());
            ModelCritique critique = objectMapper.readValue(extractJson(response), ModelCritique.class);
            return new CritiqueResult(
                    request.role(),
                    critique.score(),
                    critique.feedback(),
                    critique.evidence().stream()
                            .map(item -> new CritiqueResult.Evidence(
                                    item.documentChunkId(),
                                    item.quote(),
                                    item.reason()
                            ))
                            .toList(),
                    critique.suggestions(),
                    critique.confidence()
            );
        } catch (RuntimeException | java.io.IOException ex) {
            return fallback.review(request);
        }
    }

    private void recordTokenUsage(SpecialistReviewRequest request, TokenUsage tokenUsage) {
        if (tokenUsage == null) {
            return;
        }
        tokenUsageService.recordModelUsage(
                request.reviewTaskId(),
                request.agentRunId(),
                properties.modelName(),
                tokenUsage,
                properties.inputCostPerMillionTokens(),
                properties.outputCostPerMillionTokens()
        );
    }

    private String promptFor(SpecialistReviewRequest request) {
        return """
                You are the %s specialist in CRITIQUEBOARD.
                Review task id: %s
                Round: %d
                Assignment: %s
                
                Document chunks:
                %s

                Return JSON only with this schema:
                {
                  "score": 0,
                  "feedback": "concise review feedback",
                  "suggestions": ["actionable suggestion"],
                  "confidence": 0.0,
                  "evidence": [
                    {
                      "documentChunkId": null,
                      "quote": "source quote if available",
                      "reason": "why this evidence matters"
                    }
                  ]
                }
                """.formatted(
                request.role(),
                request.reviewTaskId(),
                request.roundNo(),
                request.inputSummary(),
                formatDocumentChunks(request)
        );
    }

    private String formatDocumentChunks(SpecialistReviewRequest request) {
        if (request.documentChunks().isEmpty()) {
            return "(No document chunks were found for this review task.)";
        }
        return request.documentChunks().stream()
                .map(chunk -> """
                        - documentChunkId: %s
                          chunkIndex: %d
                          content: %s
                        """.formatted(
                        chunk.documentChunkId(),
                        chunk.chunkIndex(),
                        chunk.content()
                ))
                .reduce("", String::concat)
                .stripTrailing();
    }

    private String extractJson(String response) {
        String trimmed = response.strip();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline >= 0 && lastFence > firstNewline) {
                return trimmed.substring(firstNewline + 1, lastFence).strip();
            }
        }
        return trimmed;
    }

    private String extractJson(ChatResponse response) {
        return extractJson(response.aiMessage().text());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ModelCritique(
            int score,
            String feedback,
            List<String> suggestions,
            double confidence,
            List<ModelEvidence> evidence
    ) {

        private ModelCritique {
            suggestions = suggestions == null ? List.of() : suggestions;
            evidence = evidence == null ? List.of() : evidence;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ModelEvidence(
            UUID documentChunkId,
            String quote,
            String reason
    ) {
    }
}
