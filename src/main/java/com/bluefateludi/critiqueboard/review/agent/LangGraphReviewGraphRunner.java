package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.progress.ReviewProgressEvent;
import com.bluefateludi.critiqueboard.review.progress.ReviewProgressPublisher;
import com.bluefateludi.critiqueboard.review.service.ReviewTaskService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LangGraphReviewGraphRunner implements ReviewGraphRunner {

    private final ReviewProgressPublisher progressPublisher;
    private final ReviewTaskService reviewTaskService;

    public LangGraphReviewGraphRunner(ReviewProgressPublisher progressPublisher, ReviewTaskService reviewTaskService) {
        this.progressPublisher = progressPublisher;
        this.reviewTaskService = reviewTaskService;
    }

    @Override
    public void run(UUID reviewTaskId) {
        reviewTaskService.markRunning(reviewTaskId);
        publish(reviewTaskId, "TASK_STARTED", "Review task started");
        reviewTaskService.markSupervising(reviewTaskId);
        publish(reviewTaskId, "SUPERVISOR_STARTED", "Supervisor started");
        reviewTaskService.markSpecialistsRunning(reviewTaskId);
        publish(reviewTaskId, "SPECIALISTS_STARTED", "Specialists started");
        reviewTaskService.markSummarizing(reviewTaskId);
        publish(reviewTaskId, "SUMMARY_STARTED", "Summary started");
        reviewTaskService.markCompleted(reviewTaskId);
        publish(reviewTaskId, "TASK_COMPLETED", "Review task completed");
    }

    private void publish(UUID reviewTaskId, String type, String message) {
        progressPublisher.publish(reviewTaskId, ReviewProgressEvent.of(type, message));
    }
}
