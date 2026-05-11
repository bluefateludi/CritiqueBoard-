package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.messaging.ReviewQueueConfig;
import com.bluefateludi.critiqueboard.review.messaging.ReviewTaskMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReviewWorker {

    private final ReviewGraphRunner reviewGraphRunner;

    public ReviewWorker(ReviewGraphRunner reviewGraphRunner) {
        this.reviewGraphRunner = reviewGraphRunner;
    }

    @RabbitListener(queues = ReviewQueueConfig.REVIEW_TASK_QUEUE)
    public void consume(ReviewTaskMessage message) {
        reviewGraphRunner.run(message.reviewTaskId());
    }
}
