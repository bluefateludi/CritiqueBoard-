package com.bluefateludi.critiqueboard.review.api.dto;

import com.bluefateludi.critiqueboard.review.domain.ReviewTaskStatus;

import java.util.UUID;

public record CreateReviewResponse(
        UUID reviewTaskId,
        ReviewTaskStatus status
) {
}
