package com.bluefateludi.critiqueboard.review.service;

import java.util.UUID;

public interface ReviewTaskPublisher {

    void publishReviewTaskCreated(UUID reviewTaskId);
}
