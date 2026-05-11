package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.ai.DeepSeekProperties;
import com.bluefateludi.critiqueboard.review.domain.AgentRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatModel;
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

    public DeepSeekSpecialistReviewer(
            ChatModel chatModel,
            ObjectMapper objectMapper,
            DeepSeekProperties properties,
            @Qualifier("deterministicSpecialistReviewer") SpecialistReviewer fallback
    ) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.fallback = fallback;
    }

    @Override
    public CritiqueResult review(SpecialistReviewRequest request) {
        if (!properties.enabled() || properties.apiKey() == null || properties.apiKey().isBlank()) {
            return fallback.review(request);
        }
        try {
            String response = chatModel.chat(promptFor(request));
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

    private String promptFor(SpecialistReviewRequest request) {
        return """
                You are the %s specialist in CRITIQUEBOARD.
                Review task id: %s
                Round: %d
                Assignment: %s

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
                request.inputSummary()
        );
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
