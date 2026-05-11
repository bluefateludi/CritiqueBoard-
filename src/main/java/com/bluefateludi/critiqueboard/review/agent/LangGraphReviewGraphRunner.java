package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.domain.AgentRole;
import com.bluefateludi.critiqueboard.review.progress.ReviewProgressEvent;
import com.bluefateludi.critiqueboard.review.progress.ReviewProgressPublisher;
import com.bluefateludi.critiqueboard.review.service.ReviewCritiqueService;
import com.bluefateludi.critiqueboard.review.service.ReviewTaskService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class LangGraphReviewGraphRunner implements ReviewGraphRunner {

    private final ReviewProgressPublisher progressPublisher;
    private final ReviewTaskService reviewTaskService;
    private final ReviewCritiqueService reviewCritiqueService;

    public LangGraphReviewGraphRunner(
            ReviewProgressPublisher progressPublisher,
            ReviewTaskService reviewTaskService,
            ReviewCritiqueService reviewCritiqueService
    ) {
        this.progressPublisher = progressPublisher;
        this.reviewTaskService = reviewTaskService;
        this.reviewCritiqueService = reviewCritiqueService;
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
        reviewCritiqueService.recordSpecialistResult(
                reviewTaskId,
                1,
                "Review document structure and information hierarchy.",
                deterministicResult(
                        AgentRole.STRUCTURE,
                        78,
                        "The document has a workable structure, but key conclusions should appear earlier.",
                        "Move the main conclusion before detailed execution notes."
                )
        );
        reviewCritiqueService.recordSpecialistResult(
                reviewTaskId,
                1,
                "Review reasoning, assumptions, and internal consistency.",
                deterministicResult(
                        AgentRole.LOGIC,
                        74,
                        "The main reasoning is understandable, but several assumptions need explicit support.",
                        "Add assumptions and decision criteria before the final recommendation."
                )
        );
        reviewCritiqueService.recordSpecialistResult(
                reviewTaskId,
                1,
                "Review risks, missing mitigations, and failure modes.",
                deterministicResult(
                        AgentRole.RISK,
                        70,
                        "The risk section needs clearer mitigation owners and trigger conditions.",
                        "Add risk owners, trigger thresholds, and rollback actions."
                )
        );
    }

    private CritiqueResult deterministicResult(AgentRole role, int score, String feedback, String suggestion) {
        return new CritiqueResult(role, score, feedback, List.of(), List.of(suggestion), 0.7);
    }
}
