package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.progress.ReviewProgressEvent;
import com.bluefateludi.critiqueboard.review.progress.ReviewProgressPublisher;
import com.bluefateludi.critiqueboard.review.chunk.DocumentChunker;
import com.bluefateludi.critiqueboard.review.repository.DocumentChunkRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewTaskRepository;
import com.bluefateludi.critiqueboard.review.service.ReviewTaskService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ReviewGraphRunnerTest {

    @Test
    void emitsInitialGraphPhasesInOrder() {
        UUID reviewTaskId = UUID.randomUUID();
        CapturingProgressPublisher progressPublisher = new CapturingProgressPublisher();
        CapturingReviewTaskService reviewTaskService = new CapturingReviewTaskService();
        ReviewGraphRunner runner = new LangGraphReviewGraphRunner(progressPublisher, reviewTaskService);

        runner.run(reviewTaskId);

        assertThat(progressPublisher.events)
                .extracting(ReviewProgressEvent::type)
                .containsExactly(
                        "TASK_STARTED",
                        "SUPERVISOR_STARTED",
                        "SPECIALISTS_STARTED",
                        "SUMMARY_STARTED",
                        "TASK_COMPLETED"
                );
        assertThat(reviewTaskService.transitions).containsExactly(
                "RUNNING",
                "SUPERVISING",
                "SPECIALISTS_RUNNING",
                "SUMMARIZING",
                "COMPLETED"
        );
    }

    private static class CapturingProgressPublisher implements ReviewProgressPublisher {
        private final List<ReviewProgressEvent> events = new CopyOnWriteArrayList<>();

        @Override
        public void publish(UUID reviewTaskId, ReviewProgressEvent event) {
            events.add(event);
        }
    }

    private static class CapturingReviewTaskService extends ReviewTaskService {
        private final List<String> transitions = new ArrayList<>();

        CapturingReviewTaskService() {
            super(
                    mock(ReviewTaskRepository.class),
                    mock(DocumentChunkRepository.class),
                    new DocumentChunker(),
                    reviewTaskId -> {
                    }
            );
        }

        @Override
        public void markRunning(UUID reviewTaskId) {
            transitions.add("RUNNING");
        }

        @Override
        public void markSupervising(UUID reviewTaskId) {
            transitions.add("SUPERVISING");
        }

        @Override
        public void markSpecialistsRunning(UUID reviewTaskId) {
            transitions.add("SPECIALISTS_RUNNING");
        }

        @Override
        public void markSummarizing(UUID reviewTaskId) {
            transitions.add("SUMMARIZING");
        }

        @Override
        public void markCompleted(UUID reviewTaskId) {
            transitions.add("COMPLETED");
        }
    }
}
