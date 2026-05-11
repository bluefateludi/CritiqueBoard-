package com.bluefateludi.critiqueboard.review.domain;

public record CreateReviewCommand(
        String title,
        String originalText,
        String requirement,
        boolean secondRoundEnabled
) {
}
