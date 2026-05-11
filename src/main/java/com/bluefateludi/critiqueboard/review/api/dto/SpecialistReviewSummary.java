package com.bluefateludi.critiqueboard.review.api.dto;

import com.bluefateludi.critiqueboard.review.domain.AgentRole;

import java.util.List;

public record SpecialistReviewSummary(
        AgentRole role,
        int score,
        String feedback,
        List<String> suggestions,
        double confidence
) {
}
