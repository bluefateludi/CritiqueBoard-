package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.progress.ReviewProgressEvent;
import com.bluefateludi.critiqueboard.review.progress.ReviewProgressPublisher;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewGraphRunnerTest {

    @Test
    void emitsInitialGraphPhasesInOrder() {
        UUID reviewTaskId = UUID.randomUUID();
        CapturingProgressPublisher progressPublisher = new CapturingProgressPublisher();
        ReviewGraphRunner runner = new LangGraphReviewGraphRunner(progressPublisher);

        runner.run(reviewTaskId);

        assertThat(progressPublisher.events)
                .extracting(ReviewProgressEvent::type)
                .containsExactly(
                        "TASK_STARTED",
                        "SUPERVISOR_STARTED",
                        "SPECIALISTS_STARTED",
                        "SUMMARY_STARTED",
                        "TASK_COMPLETED"
                );
    }

    private static class CapturingProgressPublisher implements ReviewProgressPublisher {
        private final List<ReviewProgressEvent> events = new CopyOnWriteArrayList<>();

        @Override
        public void publish(UUID reviewTaskId, ReviewProgressEvent event) {
            events.add(event);
        }
    }
}
