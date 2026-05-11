package com.bluefateludi.critiqueboard.review.progress;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ReviewEventService {

    private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emittersByTask = new ConcurrentHashMap<>();

    public SseEmitter subscribe(UUID reviewTaskId) {
        SseEmitter emitter = new SseEmitter(0L);
        subscribe(reviewTaskId, emitter);
        return emitter;
    }

    void subscribe(UUID reviewTaskId, SseEmitter emitter) {
        emittersByTask.computeIfAbsent(reviewTaskId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(reviewTaskId, emitter));
        emitter.onTimeout(() -> remove(reviewTaskId, emitter));
        emitter.onError(error -> remove(reviewTaskId, emitter));
    }

    public void emit(UUID reviewTaskId, ReviewProgressEvent event) {
        List<SseEmitter> emitters = emittersByTask.getOrDefault(reviewTaskId, new CopyOnWriteArrayList<>());
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(event.type())
                        .data(event));
            } catch (IOException | IllegalStateException ex) {
                remove(reviewTaskId, emitter);
            }
        }
    }

    private void remove(UUID reviewTaskId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByTask.get(reviewTaskId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByTask.remove(reviewTaskId);
        }
    }
}
