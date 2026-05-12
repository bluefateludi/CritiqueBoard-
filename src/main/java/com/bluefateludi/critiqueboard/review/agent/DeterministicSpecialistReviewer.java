package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.domain.AgentRole;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeterministicSpecialistReviewer implements SpecialistReviewer {

    @Override
    public CritiqueResult review(SpecialistReviewRequest request) {
        return switch (request.role()) {
            case STRUCTURE -> result(
                    request,
                    AgentRole.STRUCTURE,
                    78,
                    "The document has a workable structure, but key conclusions should appear earlier.",
                    "Move the main conclusion before detailed execution notes."
            );
            case LOGIC -> result(
                    request,
                    AgentRole.LOGIC,
                    74,
                    "The main reasoning is understandable, but several assumptions need explicit support.",
                    "Add assumptions and decision criteria before the final recommendation."
            );
            case RISK -> result(
                    request,
                    AgentRole.RISK,
                    70,
                    "The risk section needs clearer mitigation owners and trigger conditions.",
                    "Add risk owners, trigger thresholds, and rollback actions."
            );
            case SUPERVISOR -> throw new IllegalArgumentException("Supervisor is not a specialist role.");
        };
    }

    private CritiqueResult result(SpecialistReviewRequest request, AgentRole role, int score, String feedback, String suggestion) {
        return new CritiqueResult(role, score, feedback, evidenceFrom(request), List.of(suggestion), 0.7);
    }

    private List<CritiqueResult.Evidence> evidenceFrom(SpecialistReviewRequest request) {
        if (request.documentChunks().isEmpty()) {
            return List.of();
        }
        DocumentChunkContext chunk = request.documentChunks().getFirst();
        return List.of(new CritiqueResult.Evidence(
                chunk.documentChunkId(),
                excerpt(chunk.content()),
                "Fallback reviewer based this critique on the available document chunk."
        ));
    }

    private String excerpt(String content) {
        String normalized = content == null ? "" : content.strip();
        if (normalized.length() <= 240) {
            return normalized;
        }
        return normalized.substring(0, 240);
    }
}
