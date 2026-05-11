package com.bluefateludi.critiqueboard.review.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateReviewRequest(
        String title,
        @NotBlank String text,
        @NotBlank String requirement,
        Boolean secondRoundEnabled
) {

    public boolean secondRoundEnabledOrDefault() {
        return Boolean.TRUE.equals(secondRoundEnabled);
    }
}
