package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.domain.AgentRole;

import java.util.List;
import java.util.UUID;

public record CritiqueResult(
        AgentRole role,
        int score,
        String feedback,
        List<Evidence> evidence,
        List<String> suggestions,
        double confidence
) {

    public record Evidence(
            UUID documentChunkId,
            String quote,
            String reason
    ) {
    }
}
