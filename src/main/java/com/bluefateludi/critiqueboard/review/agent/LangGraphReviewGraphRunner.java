package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.progress.ReviewProgressEvent;
import com.bluefateludi.critiqueboard.review.progress.ReviewProgressPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LangGraphReviewGraphRunner implements ReviewGraphRunner {

    private final ReviewProgressPublisher progressPublisher;

    public LangGraphReviewGraphRunner(ReviewProgressPublisher progressPublisher) {
        this.progressPublisher = progressPublisher;
    }

    @Override
    public void run(UUID reviewTaskId) {
        publish(reviewTaskId, "TASK_STARTED", "Review task started");
        publish(reviewTaskId, "SUPERVISOR_STARTED", "Supervisor started");
        publish(reviewTaskId, "SPECIALISTS_STARTED", "Specialists started");
        publish(reviewTaskId, "SUMMARY_STARTED", "Summary started");
        publish(reviewTaskId, "TASK_COMPLETED", "Review task completed");
    }

    private void publish(UUID reviewTaskId, String type, String message) {
        progressPublisher.publish(reviewTaskId, ReviewProgressEvent.of(type, message));
    }
}
