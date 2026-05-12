package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.messaging.ReviewTaskMessage;
import com.bluefateludi.critiqueboard.review.progress.ReviewProgressEvent;
import com.bluefateludi.critiqueboard.review.progress.ReviewProgressPublisher;
import com.bluefateludi.critiqueboard.review.service.ReviewTaskService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

class ReviewWorkerTest {

    @Test
    void marksTaskFailedAndPublishesFailureWhenGraphRunnerThrows() {
        UUID reviewTaskId = UUID.randomUUID();
        CapturingReviewTaskService reviewTaskService = new CapturingReviewTaskService();
        CapturingProgressPublisher progressPublisher = new CapturingProgressPublisher();
        ReviewWorker worker = new ReviewWorker(
                id -> {
                    throw new IllegalStateException("Worker crashed");
                },
                reviewTaskService,
                progressPublisher
        );

        worker.consume(new ReviewTaskMessage(reviewTaskId));

        assertThat(reviewTaskService.failedReviewTaskIds).containsExactly(reviewTaskId);
        assertThat(reviewTaskService.failureMessages).containsExactly("Worker crashed");
        assertThat(progressPublisher.events)
                .extracting(ReviewProgressEvent::type)
                .containsExactly("TASK_FAILED");
    }

    @Test
    void publishesFailureWithoutRethrowingWhenTaskCannotBeMarkedFailed() {
        UUID reviewTaskId = UUID.randomUUID();
        ThrowingReviewTaskService reviewTaskService = new ThrowingReviewTaskService();
        CapturingProgressPublisher progressPublisher = new CapturingProgressPublisher();
        ReviewWorker worker = new ReviewWorker(
                id -> {
                    throw new IllegalArgumentException("Review task not found: " + id);
                },
                reviewTaskService,
                progressPublisher
        );

        assertThatCode(() -> worker.consume(new ReviewTaskMessage(reviewTaskId)))
                .doesNotThrowAnyException();
        assertThat(progressPublisher.events)
                .extracting(ReviewProgressEvent::type)
                .containsExactly("TASK_FAILED");
    }

    private static class CapturingReviewTaskService extends ReviewTaskService {
        private final List<UUID> failedReviewTaskIds = new ArrayList<>();
        private final List<String> failureMessages = new ArrayList<>();

        CapturingReviewTaskService() {
            super(mock(), mock(), mock(), mock(), mock(), reviewTaskId -> {
            });
        }

        @Override
        public void markFailed(UUID reviewTaskId, String errorMessage) {
            failedReviewTaskIds.add(reviewTaskId);
            failureMessages.add(errorMessage);
        }
    }

    private static class CapturingProgressPublisher implements ReviewProgressPublisher {
        private final List<ReviewProgressEvent> events = new ArrayList<>();

        @Override
        public void publish(UUID reviewTaskId, ReviewProgressEvent event) {
            events.add(event);
        }
    }

    private static class ThrowingReviewTaskService extends ReviewTaskService {

        ThrowingReviewTaskService() {
            super(mock(), mock(), mock(), mock(), mock(), reviewTaskId -> {
            });
        }

        @Override
        public void markFailed(UUID reviewTaskId, String errorMessage) {
            throw new IllegalArgumentException("Review task not found: " + reviewTaskId);
        }
    }
}
