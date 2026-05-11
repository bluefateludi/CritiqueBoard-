package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.progress.ReviewProgressEvent;
import com.bluefateludi.critiqueboard.review.progress.ReviewProgressPublisher;
import com.bluefateludi.critiqueboard.review.chunk.DocumentChunker;
import com.bluefateludi.critiqueboard.review.domain.AgentRole;
import com.bluefateludi.critiqueboard.review.repository.DocumentChunkRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewCritiqueRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewTaskRepository;
import com.bluefateludi.critiqueboard.review.service.ReviewCritiqueService;
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
        CapturingReviewCritiqueService critiqueService = new CapturingReviewCritiqueService();
        ReviewGraphRunner runner = new LangGraphReviewGraphRunner(progressPublisher, reviewTaskService, critiqueService);

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
        assertThat(critiqueService.roles).containsExactly(AgentRole.STRUCTURE, AgentRole.LOGIC, AgentRole.RISK);
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
                    mock(ReviewCritiqueRepository.class),
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

    private static class CapturingReviewCritiqueService extends ReviewCritiqueService {
        private final List<AgentRole> roles = new ArrayList<>();

        CapturingReviewCritiqueService() {
            super(mock(ReviewTaskRepository.class), mock(), mock());
        }

        @Override
        public com.bluefateludi.critiqueboard.review.domain.ReviewCritique recordSpecialistResult(
                UUID reviewTaskId,
                int roundNo,
                String inputSummary,
                CritiqueResult result
        ) {
            roles.add(result.role());
            return null;
        }
    }
}
