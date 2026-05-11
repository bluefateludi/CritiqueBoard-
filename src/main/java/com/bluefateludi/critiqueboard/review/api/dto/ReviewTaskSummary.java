package com.bluefateludi.critiqueboard.review.api.dto;

import com.bluefateludi.critiqueboard.review.domain.ReviewTaskStatus;

import java.util.List;
import java.util.UUID;

public record ReviewTaskSummary(
        UUID reviewTaskId,
        String title,
        ReviewTaskStatus status,
        List<SpecialistReviewSummary> specialistReviews
) {

    public ReviewTaskSummary(UUID reviewTaskId, String title, ReviewTaskStatus status) {
        this(reviewTaskId, title, status, List.of());
    }
}
