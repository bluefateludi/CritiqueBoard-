package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.domain.AgentRole;

import java.util.List;
import java.util.UUID;

public record SpecialistReviewRequest(
        UUID reviewTaskId,
        UUID agentRunId,
        AgentRole role,
        int roundNo,
        String inputSummary,
        List<DocumentChunkContext> documentChunks
) {
    public SpecialistReviewRequest(
            UUID reviewTaskId,
            UUID agentRunId,
            AgentRole role,
            int roundNo,
            String inputSummary
    ) {
        this(reviewTaskId, agentRunId, role, roundNo, inputSummary, List.of());
    }

    public SpecialistReviewRequest {
        documentChunks = documentChunks == null ? List.of() : List.copyOf(documentChunks);
    }
}
