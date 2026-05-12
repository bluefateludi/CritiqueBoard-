package com.bluefateludi.critiqueboard.review.progress;

import com.bluefateludi.critiqueboard.review.domain.ReviewEvent;
import com.bluefateludi.critiqueboard.review.repository.ReviewEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewEventServiceTest {

    @Test
    void emitsEventToSubscribersForSameReviewTask() {
        ReviewEventService service = new ReviewEventService(emptyRepository());
        UUID reviewTaskId = UUID.randomUUID();
        CapturingEmitter emitter = new CapturingEmitter();

        service.subscribe(reviewTaskId, emitter);
        service.emit(reviewTaskId, ReviewProgressEvent.of("TASK_STARTED", "Review started"));

        assertThat(emitter.events).hasSize(1);
    }

    @Test
    void doesNotEmitToDifferentReviewTaskSubscribers() {
        ReviewEventService service = new ReviewEventService(emptyRepository());
        CapturingEmitter emitter = new CapturingEmitter();

        service.subscribe(UUID.randomUUID(), emitter);
        service.emit(UUID.randomUUID(), ReviewProgressEvent.of("TASK_STARTED", "Review started"));

        assertThat(emitter.events).isEmpty();
    }

    @Test
    void persistsPublishedEvents() {
        ReviewEventRepository repository = emptyRepository();
        ReviewEventService service = new ReviewEventService(repository);
        UUID reviewTaskId = UUID.randomUUID();

        service.publish(reviewTaskId, new ReviewProgressEvent(
                "TASK_STARTED",
                "Review started",
                Map.of("step", "queued"),
                java.time.OffsetDateTime.now()
        ));

        verify(repository).save(any(ReviewEvent.class));
    }

    @Test
    void replaysPersistedEventsWhenSubscribing() {
        ReviewEventRepository repository = emptyRepository();
        UUID reviewTaskId = UUID.randomUUID();
        when(repository.findByReviewTaskIdOrderByCreatedAtAsc(reviewTaskId))
                .thenReturn(List.of(ReviewEvent.from(
                        reviewTaskId,
                        ReviewProgressEvent.of("TASK_STARTED", "Review started")
                )));
        ReviewEventService service = new ReviewEventService(repository);
        CapturingEmitter emitter = new CapturingEmitter();

        service.subscribe(reviewTaskId, emitter);

        assertThat(emitter.events).hasSize(1);
    }

    @Test
    void stillEmitsLiveEventWhenPersistenceFails() {
        ReviewEventRepository repository = emptyRepository();
        when(repository.save(any(ReviewEvent.class))).thenThrow(new IllegalStateException("database unavailable"));
        ReviewEventService service = new ReviewEventService(repository);
        UUID reviewTaskId = UUID.randomUUID();
        CapturingEmitter emitter = new CapturingEmitter();
        service.subscribe(reviewTaskId, emitter);

        service.publish(reviewTaskId, ReviewProgressEvent.of("TASK_FAILED", "Worker crashed"));

        assertThat(emitter.events).hasSize(1);
    }

    @Test
    void subscribesEvenWhenHistoryReplayFails() {
        ReviewEventRepository repository = emptyRepository();
        UUID reviewTaskId = UUID.randomUUID();
        when(repository.findByReviewTaskIdOrderByCreatedAtAsc(reviewTaskId))
                .thenThrow(new IllegalStateException("database unavailable"));
        ReviewEventService service = new ReviewEventService(repository);
        CapturingEmitter emitter = new CapturingEmitter();

        service.subscribe(reviewTaskId, emitter);
        service.publish(reviewTaskId, ReviewProgressEvent.of("TASK_STARTED", "Review started"));

        assertThat(emitter.events).hasSize(1);
    }

    private ReviewEventRepository emptyRepository() {
        ReviewEventRepository repository = mock(ReviewEventRepository.class);
        when(repository.save(any(ReviewEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findByReviewTaskIdOrderByCreatedAtAsc(any())).thenReturn(List.of());
        return repository;
    }

    private static class CapturingEmitter extends SseEmitter {
        private final List<SseEventBuilder> events = new CopyOnWriteArrayList<>();

        @Override
        public void send(SseEventBuilder builder) {
            events.add(builder);
        }
    }
}
