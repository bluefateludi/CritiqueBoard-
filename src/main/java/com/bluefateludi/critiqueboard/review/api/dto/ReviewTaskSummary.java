package com.bluefateludi.critiqueboard.review.api.dto;

import com.bluefateludi.critiqueboard.review.domain.ReviewTaskStatus;

import java.util.List;
import java.util.UUID;

public record ReviewTaskSummary(
        UUID reviewTaskId,
        String title,
        ReviewTaskStatus status,
        String errorMessage,
        ReviewReportSummary report,
        List<SpecialistReviewSummary> specialistReviews
) {

    public ReviewTaskSummary(UUID reviewTaskId, String title, ReviewTaskStatus status) {
        this(reviewTaskId, title, status, null, null, List.of());
    }

    public ReviewTaskSummary(
            UUID reviewTaskId,
            String title,
            ReviewTaskStatus status,
            ReviewReportSummary report,
            List<SpecialistReviewSummary> specialistReviews
    ) {
        this(reviewTaskId, title, status, null, report, specialistReviews);
    }
}
