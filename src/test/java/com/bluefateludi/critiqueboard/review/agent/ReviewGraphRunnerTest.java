package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.progress.ReviewProgressEvent;
import com.bluefateludi.critiqueboard.review.progress.ReviewProgressPublisher;
import com.bluefateludi.critiqueboard.review.chunk.DocumentChunker;
import com.bluefateludi.critiqueboard.review.domain.AgentRole;
import com.bluefateludi.critiqueboard.review.domain.AgentRun;
import com.bluefateludi.critiqueboard.review.domain.ReviewTask;
import com.bluefateludi.critiqueboard.review.repository.DocumentChunkRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewCritiqueRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewTaskRepository;
import com.bluefateludi.critiqueboard.review.service.AgentRunService;
import com.bluefateludi.critiqueboard.review.service.ReviewCritiqueService;
import com.bluefateludi.critiqueboard.review.service.ReviewTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
        CapturingAgentRunService agentRunService = new CapturingAgentRunService();
        CapturingReviewCritiqueService critiqueService = new CapturingReviewCritiqueService();
        CapturingSpecialistReviewer specialistReviewer = new CapturingSpecialistReviewer();
        ReviewGraphRunner runner = new LangGraphReviewGraphRunner(
                progressPublisher,
                reviewTaskService,
                agentRunService,
                critiqueService,
                specialistReviewer
        );

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
        assertThat(agentRunService.startedRoles).containsExactly(AgentRole.STRUCTURE, AgentRole.LOGIC, AgentRole.RISK);
        assertThat(agentRunService.completedRunIds).containsExactlyElementsOf(agentRunService.startedRunIds);
        assertThat(critiqueService.roles).containsExactly(AgentRole.STRUCTURE, AgentRole.LOGIC, AgentRole.RISK);
        assertThat(critiqueService.agentRunIds).containsExactlyElementsOf(agentRunService.startedRunIds);
        assertThat(specialistReviewer.roles).containsExactly(AgentRole.STRUCTURE, AgentRole.LOGIC, AgentRole.RISK);
        assertThat(specialistReviewer.agentRunIds).containsExactlyElementsOf(agentRunService.startedRunIds);
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
        private final List<UUID> agentRunIds = new ArrayList<>();

        CapturingReviewCritiqueService() {
            super(mock(ReviewTaskRepository.class), mock(), mock());
        }

        @Override
        public com.bluefateludi.critiqueboard.review.domain.ReviewCritique recordSpecialistResult(
                UUID reviewTaskId,
                UUID agentRunId,
                CritiqueResult result
        ) {
            roles.add(result.role());
            agentRunIds.add(agentRunId);
            return null;
        }
    }

    private static class CapturingSpecialistReviewer implements SpecialistReviewer {
        private final List<AgentRole> roles = new ArrayList<>();
        private final List<UUID> agentRunIds = new ArrayList<>();

        @Override
        public CritiqueResult review(SpecialistReviewRequest request) {
            roles.add(request.role());
            agentRunIds.add(request.agentRunId());
            return new CritiqueResult(
                    request.role(),
                    80,
                    "Captured " + request.role() + " feedback.",
                    List.of(),
                    List.of("Captured suggestion."),
                    0.8
            );
        }
    }

    private static class CapturingAgentRunService extends AgentRunService {
        private final ReviewTask task = ReviewTask.create("Launch Plan", "Text", "Requirement", true);
        private final List<AgentRole> startedRoles = new ArrayList<>();
        private final List<UUID> startedRunIds = new ArrayList<>();
        private final List<UUID> completedRunIds = new ArrayList<>();

        CapturingAgentRunService() {
            super(mock(ReviewTaskRepository.class), mock());
        }

        @Override
        public AgentRun startSpecialistRun(UUID reviewTaskId, AgentRole role, int roundNo, String inputSummary) {
            UUID agentRunId = UUID.randomUUID();
            AgentRun run = AgentRun.create(task, role, roundNo, inputSummary);
            run.markRunning();
            ReflectionTestUtils.setField(run, "id", agentRunId);
            startedRoles.add(role);
            startedRunIds.add(agentRunId);
            return run;
        }

        @Override
        public void completeRun(UUID agentRunId, String outputSummary) {
            completedRunIds.add(agentRunId);
        }
    }
}
