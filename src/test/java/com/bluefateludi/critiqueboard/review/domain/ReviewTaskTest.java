package com.bluefateludi.critiqueboard.review.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewTaskTest {

    @Test
    void createStartsPendingAndKeepsSubmissionFields() {
        CreateReviewCommand command = new CreateReviewCommand(
                "Launch Plan",
                "We will launch the product in Q3.",
                "Review structure, logic, and risk.",
                true
        );
        ReviewTask task = ReviewTask.create(command);

        assertThat(task.getStatus()).isEqualTo(ReviewTaskStatus.PENDING);
        assertThat(task.getTitle()).isEqualTo("Launch Plan");
        assertThat(task.getOriginalText()).isEqualTo("We will launch the product in Q3.");
        assertThat(task.getRequirement()).isEqualTo("Review structure, logic, and risk.");
        assertThat(task.isSecondRoundEnabled()).isTrue();
    }
}
