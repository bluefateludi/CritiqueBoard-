package com.bluefateludi.critiqueboard.review.progress;

import java.time.OffsetDateTime;
import java.util.Map;

public record ReviewProgressEvent(
        String type,
        String message,
        Map<String, Object> payload,
        OffsetDateTime occurredAt
) {

    public static ReviewProgressEvent of(String type, String message) {
        return new ReviewProgressEvent(type, message, Map.of(), OffsetDateTime.now());
    }
}
