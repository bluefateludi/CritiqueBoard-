package com.bluefateludi.critiqueboard.review.service;

import com.bluefateludi.critiqueboard.review.agent.CritiqueResult;
import com.bluefateludi.critiqueboard.review.domain.AgentRun;
import com.bluefateludi.critiqueboard.review.domain.ReviewCritique;
import com.bluefateludi.critiqueboard.review.domain.ReviewTask;
import com.bluefateludi.critiqueboard.review.repository.AgentRunRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewCritiqueRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReviewCritiqueService {

    private final ReviewTaskRepository reviewTaskRepository;
    private final AgentRunRepository agentRunRepository;
    private final ReviewCritiqueRepository reviewCritiqueRepository;

    public ReviewCritiqueService(
            ReviewTaskRepository reviewTaskRepository,
            AgentRunRepository agentRunRepository,
            ReviewCritiqueRepository reviewCritiqueRepository
    ) {
        this.reviewTaskRepository = reviewTaskRepository;
        this.agentRunRepository = agentRunRepository;
        this.reviewCritiqueRepository = reviewCritiqueRepository;
    }

    @Transactional
    public ReviewCritique recordSpecialistResult(
            UUID reviewTaskId,
            int roundNo,
            String inputSummary,
            CritiqueResult result
    ) {
        ReviewTask task = reviewTaskRepository.findById(reviewTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Review task not found: " + reviewTaskId));
        AgentRun run = AgentRun.create(task, result.role(), roundNo, inputSummary);
        run.markRunning();
        run.markCompleted(result.feedback());
        AgentRun savedRun = agentRunRepository.save(run);
        ReviewCritique critique = ReviewCritique.from(task, savedRun, result);
        return reviewCritiqueRepository.save(critique);
    }
}
