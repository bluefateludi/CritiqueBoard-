package com.bluefateludi.critiqueboard.review.messaging;

import java.util.UUID;

public record ReviewTaskMessage(UUID reviewTaskId) {

    public ReviewTaskMessage {
        if (reviewTaskId == null) {
            throw new IllegalArgumentException("reviewTaskId is required");
        }
    }
}
