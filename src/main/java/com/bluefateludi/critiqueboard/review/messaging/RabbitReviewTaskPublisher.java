package com.bluefateludi.critiqueboard.review.messaging;

import com.bluefateludi.critiqueboard.review.service.ReviewTaskPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RabbitReviewTaskPublisher implements ReviewTaskPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitReviewTaskPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishReviewTaskCreated(UUID reviewTaskId) {
        rabbitTemplate.convertAndSend(
                ReviewQueueConfig.REVIEW_EXCHANGE,
                ReviewQueueConfig.REVIEW_TASK_CREATED_ROUTING_KEY,
                new ReviewTaskMessage(reviewTaskId)
        );
    }
}
