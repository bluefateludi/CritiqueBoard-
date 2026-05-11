package com.bluefateludi.critiqueboard.review.messaging;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewTaskMessageTest {

    @Test
    void requiresReviewTaskId() {
        assertThatThrownBy(() -> new ReviewTaskMessage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("reviewTaskId is required");
    }

    @Test
    void storesReviewTaskId() {
        UUID reviewTaskId = UUID.randomUUID();

        ReviewTaskMessage message = new ReviewTaskMessage(reviewTaskId);

        assertThat(message.reviewTaskId()).isEqualTo(reviewTaskId);
    }
}
