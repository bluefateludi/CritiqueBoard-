package com.bluefateludi.critiqueboard.review.service;

import com.bluefateludi.critiqueboard.review.domain.AgentRole;
import com.bluefateludi.critiqueboard.review.domain.AgentRun;
import com.bluefateludi.critiqueboard.review.domain.AgentRunStatus;
import com.bluefateludi.critiqueboard.review.domain.ReviewTask;
import com.bluefateludi.critiqueboard.review.repository.AgentRunRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewTaskRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentRunServiceTest {

    @Test
    void startsSpecialistRunForReviewTask() {
        ReviewTaskRepository reviewTaskRepository = mock(ReviewTaskRepository.class);
        AgentRunRepository agentRunRepository = mock(AgentRunRepository.class);
        ReviewTask task = ReviewTask.create(
                "Launch Plan",
                "We will launch the product in Q3.",
                "Review structure, logic, and risk.",
                true
        );
        UUID reviewTaskId = UUID.randomUUID();
        when(reviewTaskRepository.findById(reviewTaskId)).thenReturn(Optional.of(task));
        when(agentRunRepository.save(any(AgentRun.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AgentRunService service = new AgentRunService(reviewTaskRepository, agentRunRepository);

        AgentRun run = service.startSpecialistRun(
                reviewTaskId,
                AgentRole.STRUCTURE,
                1,
                "Review document structure."
        );

        assertThat(run.getReviewTask()).isSameAs(task);
        assertThat(run.getRole()).isEqualTo(AgentRole.STRUCTURE);
        assertThat(run.getRoundNo()).isEqualTo(1);
        assertThat(run.getInputSummary()).isEqualTo("Review document structure.");
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.RUNNING);
        assertThat(run.getStartedAt()).isNotNull();
        verify(agentRunRepository).save(run);
    }

    @Test
    void completesExistingRun() {
        ReviewTaskRepository reviewTaskRepository = mock(ReviewTaskRepository.class);
        AgentRunRepository agentRunRepository = mock(AgentRunRepository.class);
        ReviewTask task = ReviewTask.create(
                "Launch Plan",
                "We will launch the product in Q3.",
                "Review structure, logic, and risk.",
                true
        );
        AgentRun run = AgentRun.create(task, AgentRole.LOGIC, 1, "Review logic.");
        UUID agentRunId = UUID.randomUUID();
        when(agentRunRepository.findById(agentRunId)).thenReturn(Optional.of(run));
        AgentRunService service = new AgentRunService(reviewTaskRepository, agentRunRepository);

        service.completeRun(agentRunId, "Logic review complete.");

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.COMPLETED);
        assertThat(run.getOutputSummary()).isEqualTo("Logic review complete.");
        assertThat(run.getFinishedAt()).isNotNull();
    }

    @Test
    void failsExistingRunWithErrorMessage() {
        ReviewTaskRepository reviewTaskRepository = mock(ReviewTaskRepository.class);
        AgentRunRepository agentRunRepository = mock(AgentRunRepository.class);
        ReviewTask task = ReviewTask.create(
                "Launch Plan",
                "We will launch the product in Q3.",
                "Review structure, logic, and risk.",
                true
        );
        AgentRun run = AgentRun.create(task, AgentRole.RISK, 1, "Review risks.");
        UUID agentRunId = UUID.randomUUID();
        when(agentRunRepository.findById(agentRunId)).thenReturn(Optional.of(run));
        AgentRunService service = new AgentRunService(reviewTaskRepository, agentRunRepository);

        service.failRun(agentRunId, "LLM unavailable");

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.FAILED);
        assertThat(run.getErrorMessage()).isEqualTo("LLM unavailable");
        assertThat(run.getFinishedAt()).isNotNull();
    }
}
