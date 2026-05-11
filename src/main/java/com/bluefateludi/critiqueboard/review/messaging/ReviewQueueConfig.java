package com.bluefateludi.critiqueboard.review.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReviewQueueConfig {

    public static final String REVIEW_EXCHANGE = "review.exchange";
    public static final String REVIEW_TASK_QUEUE = "review.task.queue";
    public static final String REVIEW_TASK_CREATED_ROUTING_KEY = "review.task.created";

    @Bean
    DirectExchange reviewExchange() {
        return new DirectExchange(REVIEW_EXCHANGE, true, false);
    }

    @Bean
    Queue reviewTaskQueue() {
        return new Queue(REVIEW_TASK_QUEUE, true);
    }

    @Bean
    Binding reviewTaskBinding(Queue reviewTaskQueue, DirectExchange reviewExchange) {
        return BindingBuilder.bind(reviewTaskQueue)
                .to(reviewExchange)
                .with(REVIEW_TASK_CREATED_ROUTING_KEY);
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
