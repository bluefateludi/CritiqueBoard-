package com.bluefateludi.critiqueboard.review.api.dto;

import com.bluefateludi.critiqueboard.review.domain.ReviewTaskStatus;

import java.util.UUID;

public record ReviewTaskSummary(
        UUID reviewTaskId,
        String title,
        ReviewTaskStatus status
) {
}
