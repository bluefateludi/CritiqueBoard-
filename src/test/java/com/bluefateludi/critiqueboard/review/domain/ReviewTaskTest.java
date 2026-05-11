package com.bluefateludi.critiqueboard.review.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewTaskTest {

    @Test
    void createStartsPendingAndKeepsSubmissionFields() {
        ReviewTask task = ReviewTask.create(
                "Launch Plan",
                "We will launch the product in Q3.",
                "Review structure, logic, and risk.",
                true
        );

        assertThat(task.getStatus()).isEqualTo(ReviewTaskStatus.PENDING);
        assertThat(task.getTitle()).isEqualTo("Launch Plan");
        assertThat(task.getOriginalText()).isEqualTo("We will launch the product in Q3.");
        assertThat(task.getRequirement()).isEqualTo("Review structure, logic, and risk.");
        assertThat(task.isSecondRoundEnabled()).isTrue();
    }

    @Test
    void statusTransitionsUpdateTaskStatusAndTimestamp() {
        ReviewTask task = ReviewTask.create(
                "Launch Plan",
                "We will launch the product in Q3.",
                "Review structure, logic, and risk.",
                true
        );

        task.markRunning();
        assertThat(task.getStatus()).isEqualTo(ReviewTaskStatus.RUNNING);

        task.markSupervising();
        assertThat(task.getStatus()).isEqualTo(ReviewTaskStatus.SUPERVISING);

        task.markSpecialistsRunning();
        assertThat(task.getStatus()).isEqualTo(ReviewTaskStatus.SPECIALISTS_RUNNING);

        task.markSummarizing();
        assertThat(task.getStatus()).isEqualTo(ReviewTaskStatus.SUMMARIZING);

        task.markCompleted();
        assertThat(task.getStatus()).isEqualTo(ReviewTaskStatus.COMPLETED);
        assertThat(task.getCompletedAt()).isNotNull();
        assertThat(task.getUpdatedAt()).isEqualTo(task.getCompletedAt());
    }
}
