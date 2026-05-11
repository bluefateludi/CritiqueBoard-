package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.domain.AgentRole;
import com.bluefateludi.critiqueboard.review.domain.AgentRun;
import com.bluefateludi.critiqueboard.review.progress.ReviewProgressEvent;
import com.bluefateludi.critiqueboard.review.progress.ReviewProgressPublisher;
import com.bluefateludi.critiqueboard.review.service.AgentRunService;
import com.bluefateludi.critiqueboard.review.service.ReviewCritiqueService;
import com.bluefateludi.critiqueboard.review.service.ReviewTaskService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LangGraphReviewGraphRunner implements ReviewGraphRunner {

    private final ReviewProgressPublisher progressPublisher;
    private final ReviewTaskService reviewTaskService;
    private final AgentRunService agentRunService;
    private final ReviewCritiqueService reviewCritiqueService;
    private final SpecialistReviewer specialistReviewer;

    public LangGraphReviewGraphRunner(
            ReviewProgressPublisher progressPublisher,
            ReviewTaskService reviewTaskService,
            AgentRunService agentRunService,
            ReviewCritiqueService reviewCritiqueService,
            SpecialistReviewer specialistReviewer
    ) {
        this.progressPublisher = progressPublisher;
        this.reviewTaskService = reviewTaskService;
        this.agentRunService = agentRunService;
        this.reviewCritiqueService = reviewCritiqueService;
        this.specialistReviewer = specialistReviewer;
    }

    @Override
    public void run(UUID reviewTaskId) {
        reviewTaskService.markRunning(reviewTaskId);
        publish(reviewTaskId, "TASK_STARTED", "Review task started");
        reviewTaskService.markSupervising(reviewTaskId);
        publish(reviewTaskId, "SUPERVISOR_STARTED", "Supervisor started");
        reviewTaskService.markSpecialistsRunning(reviewTaskId);
        publish(reviewTaskId, "SPECIALISTS_STARTED", "Specialists started");
        runSpecialists(reviewTaskId);
        reviewTaskService.markSummarizing(reviewTaskId);
        publish(reviewTaskId, "SUMMARY_STARTED", "Summary started");
        reviewTaskService.markCompleted(reviewTaskId);
        publish(reviewTaskId, "TASK_COMPLETED", "Review task completed");
    }

    private void publish(UUID reviewTaskId, String type, String message) {
        progressPublisher.publish(reviewTaskId, ReviewProgressEvent.of(type, message));
    }

    private void runSpecialists(UUID reviewTaskId) {
        runSpecialist(
                reviewTaskId,
                AgentRole.STRUCTURE,
                "Review document structure and information hierarchy."
        );
        runSpecialist(
                reviewTaskId,
                AgentRole.LOGIC,
                "Review reasoning, assumptions, and internal consistency."
        );
        runSpecialist(
                reviewTaskId,
                AgentRole.RISK,
                "Review risks, missing mitigations, and failure modes."
        );
    }

    private void runSpecialist(UUID reviewTaskId, AgentRole role, String inputSummary) {
        AgentRun run = agentRunService.startSpecialistRun(reviewTaskId, role, 1, inputSummary);
        CritiqueResult result = specialistReviewer.review(new SpecialistReviewRequest(
                reviewTaskId,
                run.getId(),
                role,
                1,
                inputSummary
        ));
        reviewCritiqueService.recordSpecialistResult(reviewTaskId, run.getId(), result);
        agentRunService.completeRun(run.getId(), result.feedback());
    }
}
