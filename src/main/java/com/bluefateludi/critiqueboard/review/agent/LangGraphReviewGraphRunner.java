package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.domain.AgentRole;
import com.bluefateludi.critiqueboard.review.progress.ReviewProgressEvent;
import com.bluefateludi.critiqueboard.review.progress.ReviewProgressPublisher;
import com.bluefateludi.critiqueboard.review.service.ReviewCritiqueService;
import com.bluefateludi.critiqueboard.review.service.ReviewTaskService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LangGraphReviewGraphRunner implements ReviewGraphRunner {

    private final ReviewProgressPublisher progressPublisher;
    private final ReviewTaskService reviewTaskService;
    private final ReviewCritiqueService reviewCritiqueService;
    private final SpecialistReviewer specialistReviewer;

    public LangGraphReviewGraphRunner(
            ReviewProgressPublisher progressPublisher,
            ReviewTaskService reviewTaskService,
            ReviewCritiqueService reviewCritiqueService,
            SpecialistReviewer specialistReviewer
    ) {
        this.progressPublisher = progressPublisher;
        this.reviewTaskService = reviewTaskService;
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
        reviewCritiqueService.recordSpecialistResult(
                reviewTaskId,
                1,
                "Review document structure and information hierarchy.",
                specialistReviewer.review(new SpecialistReviewRequest(
                        reviewTaskId,
                        AgentRole.STRUCTURE,
                        1,
                        "Review document structure and information hierarchy."
                ))
        );
        reviewCritiqueService.recordSpecialistResult(
                reviewTaskId,
                1,
                "Review reasoning, assumptions, and internal consistency.",
                specialistReviewer.review(new SpecialistReviewRequest(
                        reviewTaskId,
                        AgentRole.LOGIC,
                        1,
                        "Review reasoning, assumptions, and internal consistency."
                ))
        );
        reviewCritiqueService.recordSpecialistResult(
                reviewTaskId,
                1,
                "Review risks, missing mitigations, and failure modes.",
                specialistReviewer.review(new SpecialistReviewRequest(
                        reviewTaskId,
                        AgentRole.RISK,
                        1,
                        "Review risks, missing mitigations, and failure modes."
                ))
        );
    }
}
