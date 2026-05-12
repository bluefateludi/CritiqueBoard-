package com.bluefateludi.critiqueboard.review.domain;

import com.bluefateludi.critiqueboard.review.progress.ReviewProgressEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "review_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewEvent {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "review_task_id", nullable = false)
    private UUID reviewTaskId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payloadJson = Map.of();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    private ReviewEvent(UUID reviewTaskId, ReviewProgressEvent event) {
        this.reviewTaskId = reviewTaskId;
        this.eventType = event.type();
        this.message = event.message();
        this.payloadJson = Map.copyOf(event.payload());
        this.createdAt = event.occurredAt();
    }

    public static ReviewEvent from(UUID reviewTaskId, ReviewProgressEvent event) {
        return new ReviewEvent(reviewTaskId, event);
    }

    public ReviewProgressEvent toProgressEvent() {
        return new ReviewProgressEvent(eventType, message, payloadJson, createdAt);
    }
}
