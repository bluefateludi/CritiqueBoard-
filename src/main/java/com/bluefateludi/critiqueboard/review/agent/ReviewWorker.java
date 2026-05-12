package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.messaging.ReviewQueueConfig;
import com.bluefateludi.critiqueboard.review.messaging.ReviewTaskMessage;
import com.bluefateludi.critiqueboard.review.progress.ReviewProgressEvent;
import com.bluefateludi.critiqueboard.review.progress.ReviewProgressPublisher;
import com.bluefateludi.critiqueboard.review.service.ReviewTaskService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReviewWorker {

    private final ReviewGraphRunner reviewGraphRunner;
    private final ReviewTaskService reviewTaskService;
    private final ReviewProgressPublisher progressPublisher;

    public ReviewWorker(
            ReviewGraphRunner reviewGraphRunner,
            ReviewTaskService reviewTaskService,
            ReviewProgressPublisher progressPublisher
    ) {
        this.reviewGraphRunner = reviewGraphRunner;
        this.reviewTaskService = reviewTaskService;
        this.progressPublisher = progressPublisher;
    }

    @RabbitListener(queues = ReviewQueueConfig.REVIEW_TASK_QUEUE)
    public void consume(ReviewTaskMessage message) {
        try {
            reviewGraphRunner.run(message.reviewTaskId());
        } catch (RuntimeException ex) {
            String errorMessage = errorMessage(ex);
            progressPublisher.publish(message.reviewTaskId(), ReviewProgressEvent.of("TASK_FAILED", errorMessage));
            try {
                reviewTaskService.markFailed(message.reviewTaskId(), errorMessage);
            } catch (RuntimeException ignored) {
                // The original failure has already been turned into a progress event.
            }
        }
    }

    private String errorMessage(RuntimeException ex) {
        if (ex.getMessage() == null || ex.getMessage().isBlank()) {
            return ex.getClass().getSimpleName();
        }
        return ex.getMessage();
    }
}
