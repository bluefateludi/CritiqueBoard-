package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.domain.AgentRole;

import java.util.UUID;

public record SpecialistReviewRequest(
        UUID reviewTaskId,
        AgentRole role,
        int roundNo,
        String inputSummary
) {
}
