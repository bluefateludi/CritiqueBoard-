package com.bluefateludi.critiqueboard.review.progress;

import java.util.UUID;

public interface ReviewProgressPublisher {

    void publish(UUID reviewTaskId, ReviewProgressEvent event);
}
