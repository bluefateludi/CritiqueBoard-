package com.bluefateludi.critiqueboard.review.service;

import com.bluefateludi.critiqueboard.review.domain.AgentRole;
import com.bluefateludi.critiqueboard.review.domain.AgentRun;
import com.bluefateludi.critiqueboard.review.domain.ReviewTask;
import com.bluefateludi.critiqueboard.review.repository.AgentRunRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AgentRunService {

    private final ReviewTaskRepository reviewTaskRepository;
    private final AgentRunRepository agentRunRepository;

    public AgentRunService(ReviewTaskRepository reviewTaskRepository, AgentRunRepository agentRunRepository) {
        this.reviewTaskRepository = reviewTaskRepository;
        this.agentRunRepository = agentRunRepository;
    }

    @Transactional
    public AgentRun startSpecialistRun(UUID reviewTaskId, AgentRole role, int roundNo, String inputSummary) {
        ReviewTask task = reviewTaskRepository.findById(reviewTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Review task not found: " + reviewTaskId));
        AgentRun run = AgentRun.create(task, role, roundNo, inputSummary);
        run.markRunning();
        return agentRunRepository.save(run);
    }

    @Transactional
    public void completeRun(UUID agentRunId, String outputSummary) {
        AgentRun run = agentRunRepository.findById(agentRunId)
                .orElseThrow(() -> new IllegalArgumentException("Agent run not found: " + agentRunId));
        run.markCompleted(outputSummary);
    }

    @Transactional
    public void failRun(UUID agentRunId, String errorMessage) {
        AgentRun run = agentRunRepository.findById(agentRunId)
                .orElseThrow(() -> new IllegalArgumentException("Agent run not found: " + agentRunId));
        run.markFailed(errorMessage);
    }
}
