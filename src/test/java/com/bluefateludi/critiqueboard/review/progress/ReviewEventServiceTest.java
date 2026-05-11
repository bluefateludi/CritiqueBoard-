package com.bluefateludi.critiqueboard.review.progress;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewEventServiceTest {

    @Test
    void emitsEventToSubscribersForSameReviewTask() {
        ReviewEventService service = new ReviewEventService();
        UUID reviewTaskId = UUID.randomUUID();
        CapturingEmitter emitter = new CapturingEmitter();

        service.subscribe(reviewTaskId, emitter);
        service.emit(reviewTaskId, ReviewProgressEvent.of("TASK_STARTED", "Review started"));

        assertThat(emitter.events).hasSize(1);
    }

    @Test
    void doesNotEmitToDifferentReviewTaskSubscribers() {
        ReviewEventService service = new ReviewEventService();
        CapturingEmitter emitter = new CapturingEmitter();

        service.subscribe(UUID.randomUUID(), emitter);
        service.emit(UUID.randomUUID(), ReviewProgressEvent.of("TASK_STARTED", "Review started"));

        assertThat(emitter.events).isEmpty();
    }

    private static class CapturingEmitter extends SseEmitter {
        private final List<SseEventBuilder> events = new CopyOnWriteArrayList<>();

        @Override
        public void send(SseEventBuilder builder) {
            events.add(builder);
        }
    }
}
