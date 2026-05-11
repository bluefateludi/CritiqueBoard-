package com.bluefateludi.critiqueboard.review.agent;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NoopReviewGraphRunner implements ReviewGraphRunner {

    @Override
    public void run(UUID reviewTaskId) {
        // LangGraph4j orchestration is added in the graph runner milestone.
    }
}
