package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.domain.AgentRole;
import com.bluefateludi.critiqueboard.review.domain.AgentRun;
import com.bluefateludi.critiqueboard.review.progress.ReviewProgressEvent;
import com.bluefateludi.critiqueboard.review.progress.ReviewProgressPublisher;
import com.bluefateludi.critiqueboard.review.service.AgentRunService;
import com.bluefateludi.critiqueboard.review.service.DocumentChunkContextService;
import com.bluefateludi.critiqueboard.review.service.ReviewCritiqueService;
import com.bluefateludi.critiqueboard.review.service.ReviewReportService;
import com.bluefateludi.critiqueboard.review.service.ReviewTaskService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class LangGraphReviewGraphRunner implements ReviewGraphRunner {

    private final ReviewProgressPublisher progressPublisher;
    private final ReviewTaskService reviewTaskService;
    private final AgentRunService agentRunService;
    private final ReviewCritiqueService reviewCritiqueService;
    private final ReviewReportService reviewReportService;
    private final SpecialistReviewer specialistReviewer;
    private final DocumentChunkContextService documentChunkContextService;

    public LangGraphReviewGraphRunner(
            ReviewProgressPublisher progressPublisher,
            ReviewTaskService reviewTaskService,
            AgentRunService agentRunService,
            ReviewCritiqueService reviewCritiqueService,
            ReviewReportService reviewReportService,
            SpecialistReviewer specialistReviewer,
            DocumentChunkContextService documentChunkContextService
    ) {
        this.progressPublisher = progressPublisher;
        this.reviewTaskService = reviewTaskService;
        this.agentRunService = agentRunService;
        this.reviewCritiqueService = reviewCritiqueService;
        this.reviewReportService = reviewReportService;
        this.specialistReviewer = specialistReviewer;
        this.documentChunkContextService = documentChunkContextService;
    }

    @Override
    public void run(UUID reviewTaskId) {
        try {
            reviewTaskService.markRunning(reviewTaskId);
            publish(reviewTaskId, "TASK_STARTED", "Review task started");
            reviewTaskService.markSupervising(reviewTaskId);
            publish(reviewTaskId, "SUPERVISOR_STARTED", "Supervisor started");
            reviewTaskService.markSpecialistsRunning(reviewTaskId);
            publish(reviewTaskId, "SPECIALISTS_STARTED", "Specialists started");
            runSpecialists(reviewTaskId);
            reviewTaskService.markSummarizing(reviewTaskId);
            publish(reviewTaskId, "SUMMARY_STARTED", "Summary started");
            reviewReportService.generateFinalReport(reviewTaskId);
            reviewTaskService.markCompleted(reviewTaskId);
            publish(reviewTaskId, "TASK_COMPLETED", "Review task completed");
        } catch (RuntimeException ex) {
            String message = errorMessage(ex);
            reviewTaskService.markFailed(reviewTaskId, message);
            publish(reviewTaskId, "TASK_FAILED", message);
        }
    }

    private void publish(UUID reviewTaskId, String type, String message) {
        progressPublisher.publish(reviewTaskId, ReviewProgressEvent.of(type, message));
    }

    private void runSpecialists(UUID reviewTaskId) {
        List<DocumentChunkContext> documentChunks = documentChunkContextService.getContextForReviewTask(reviewTaskId);
        runSpecialist(
                reviewTaskId,
                AgentRole.STRUCTURE,
                "Review document structure and information hierarchy.",
                documentChunks
        );
        runSpecialist(
                reviewTaskId,
                AgentRole.LOGIC,
                "Review reasoning, assumptions, and internal consistency.",
                documentChunks
        );
        runSpecialist(
                reviewTaskId,
                AgentRole.RISK,
                "Review risks, missing mitigations, and failure modes.",
                documentChunks
        );
    }

    private void runSpecialist(
            UUID reviewTaskId,
            AgentRole role,
            String inputSummary,
            List<DocumentChunkContext> documentChunks
    ) {
        AgentRun run = agentRunService.startSpecialistRun(reviewTaskId, role, 1, inputSummary);
        CritiqueResult result;
        try {
            result = specialistReviewer.review(new SpecialistReviewRequest(
                    reviewTaskId,
                    run.getId(),
                    role,
                    1,
                    inputSummary,
                    documentChunks
            ));
        } catch (RuntimeException ex) {
            agentRunService.failRun(run.getId(), errorMessage(ex));
            throw ex;
        }
        reviewCritiqueService.recordSpecialistResult(reviewTaskId, run.getId(), result);
        agentRunService.completeRun(run.getId(), result.feedback());
    }

    private String errorMessage(RuntimeException ex) {
        if (ex.getMessage() == null || ex.getMessage().isBlank()) {
            return ex.getClass().getSimpleName();
        }
        return ex.getMessage();
    }
}
