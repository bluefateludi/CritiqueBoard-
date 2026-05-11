package com.bluefateludi.critiqueboard.review.service;

import com.bluefateludi.critiqueboard.review.agent.CritiqueResult;
import com.bluefateludi.critiqueboard.review.domain.AgentRole;
import com.bluefateludi.critiqueboard.review.domain.AgentRun;
import com.bluefateludi.critiqueboard.review.domain.ReviewCritique;
import com.bluefateludi.critiqueboard.review.domain.ReviewTask;
import com.bluefateludi.critiqueboard.review.repository.AgentRunRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewCritiqueRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewTaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewCritiqueServiceTest {

    @Test
    void recordsSpecialistResultForExistingAgentRun() {
        ReviewTaskRepository taskRepository = mock(ReviewTaskRepository.class);
        AgentRunRepository agentRunRepository = mock(AgentRunRepository.class);
        ReviewCritiqueRepository critiqueRepository = mock(ReviewCritiqueRepository.class);
        ReviewTask task = ReviewTask.create(
                "Launch Plan",
                "We will launch the product in Q3.",
                "Review structure, logic, and risk.",
                true
        );
        UUID reviewTaskId = UUID.randomUUID();
        UUID agentRunId = UUID.randomUUID();
        AgentRun run = AgentRun.create(task, AgentRole.LOGIC, 1, "Logic specialist task");
        run.markRunning();
        ReflectionTestUtils.setField(run, "id", agentRunId);
        when(taskRepository.findById(reviewTaskId)).thenReturn(Optional.of(task));
        when(agentRunRepository.findById(agentRunId)).thenReturn(Optional.of(run));
        when(critiqueRepository.save(any(ReviewCritique.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ReviewCritiqueService service = new ReviewCritiqueService(taskRepository, agentRunRepository, critiqueRepository);
        CritiqueResult result = new CritiqueResult(
                AgentRole.LOGIC,
                80,
                "Argument is mostly coherent.",
                List.of(),
                List.of("Add decision criteria."),
                0.9
        );

        ReviewCritique critique = service.recordSpecialistResult(reviewTaskId, agentRunId, result);

        assertThat(critique.getReviewTask()).isSameAs(task);
        assertThat(critique.getRole()).isEqualTo(AgentRole.LOGIC);
        assertThat(critique.getAgentRun()).isSameAs(run);
        verify(agentRunRepository).findById(agentRunId);
        verify(critiqueRepository).save(critique);
    }
}
