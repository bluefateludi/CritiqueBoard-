package com.bluefateludi.critiqueboard.review.service;

import com.bluefateludi.critiqueboard.review.api.dto.ReviewTaskSummary;
import com.bluefateludi.critiqueboard.review.api.dto.ReviewReportSummary;
import com.bluefateludi.critiqueboard.review.api.dto.SpecialistReviewSummary;
import com.bluefateludi.critiqueboard.review.chunk.DocumentChunker;
import com.bluefateludi.critiqueboard.review.domain.DocumentChunk;
import com.bluefateludi.critiqueboard.review.domain.ReviewCritique;
import com.bluefateludi.critiqueboard.review.domain.ReviewReport;
import com.bluefateludi.critiqueboard.review.domain.ReviewTask;
import com.bluefateludi.critiqueboard.review.repository.DocumentChunkRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewCritiqueRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewReportRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class ReviewTaskService {

    private final ReviewTaskRepository reviewTaskRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final ReviewCritiqueRepository reviewCritiqueRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final DocumentChunker documentChunker;
    private final ReviewTaskPublisher reviewTaskPublisher;

    public ReviewTaskService(
            ReviewTaskRepository reviewTaskRepository,
            DocumentChunkRepository documentChunkRepository,
            ReviewCritiqueRepository reviewCritiqueRepository,
            ReviewReportRepository reviewReportRepository,
            DocumentChunker documentChunker,
            ReviewTaskPublisher reviewTaskPublisher
    ) {
        this.reviewTaskRepository = reviewTaskRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.reviewCritiqueRepository = reviewCritiqueRepository;
        this.reviewReportRepository = reviewReportRepository;
        this.documentChunker = documentChunker;
        this.reviewTaskPublisher = reviewTaskPublisher;
    }

    @Transactional
    public UUID createReview(String title, String originalText, String requirement, boolean secondRoundEnabled) {
        ReviewTask task = ReviewTask.create(title, originalText, requirement, secondRoundEnabled);
        ReviewTask saved = reviewTaskRepository.save(task);
        List<DocumentChunk> chunks = documentChunker.chunk(originalText).stream()
                .map(chunk -> DocumentChunk.create(saved, chunk.index(), chunk.content()))
                .toList();
        documentChunkRepository.saveAll(chunks);
        publishTaskCreatedAfterCommit(saved.getId());
        return saved.getId();
    }

    @Transactional(readOnly = true)
    public ReviewTaskSummary getReview(UUID reviewTaskId) {
        ReviewTask task = reviewTaskRepository.findById(reviewTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Review task not found: " + reviewTaskId));
        List<SpecialistReviewSummary> specialistReviews = reviewCritiqueRepository
                .findByReviewTaskIdOrderByCreatedAtAsc(reviewTaskId)
                .stream()
                .map(this::toSpecialistSummary)
                .toList();
        ReviewReportSummary report = reviewReportRepository.findByReviewTaskId(reviewTaskId)
                .map(this::toReportSummary)
                .orElse(null);
        return new ReviewTaskSummary(
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                task.getErrorMessage(),
                report,
                specialistReviews
        );
    }

    @Transactional
    public void markRunning(UUID reviewTaskId) {
        updateTask(reviewTaskId, ReviewTask::markRunning);
    }

    @Transactional
    public void markSupervising(UUID reviewTaskId) {
        updateTask(reviewTaskId, ReviewTask::markSupervising);
    }

    @Transactional
    public void markSpecialistsRunning(UUID reviewTaskId) {
        updateTask(reviewTaskId, ReviewTask::markSpecialistsRunning);
    }

    @Transactional
    public void markSummarizing(UUID reviewTaskId) {
        updateTask(reviewTaskId, ReviewTask::markSummarizing);
    }

    @Transactional
    public void markCompleted(UUID reviewTaskId) {
        updateTask(reviewTaskId, ReviewTask::markCompleted);
    }

    @Transactional
    public void markFailed(UUID reviewTaskId, String errorMessage) {
        ReviewTask task = reviewTaskRepository.findById(reviewTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Review task not found: " + reviewTaskId));
        task.markFailed(errorMessage);
    }

    private void updateTask(UUID reviewTaskId, Consumer<ReviewTask> transition) {
        ReviewTask task = reviewTaskRepository.findById(reviewTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Review task not found: " + reviewTaskId));
        transition.accept(task);
    }

    private void publishTaskCreatedAfterCommit(UUID reviewTaskId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            reviewTaskPublisher.publishReviewTaskCreated(reviewTaskId);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                reviewTaskPublisher.publishReviewTaskCreated(reviewTaskId);
            }
        });
    }

    private SpecialistReviewSummary toSpecialistSummary(ReviewCritique critique) {
        return new SpecialistReviewSummary(
                critique.getRole(),
                critique.getScore(),
                critique.getFeedback(),
                critique.getSuggestions(),
                critique.getConfidence().doubleValue()
        );
    }

    private ReviewReportSummary toReportSummary(ReviewReport report) {
        return new ReviewReportSummary(
                report.getOverallScore(),
                report.getExecutiveSummary(),
                report.getStrengths(),
                report.getWeaknesses(),
                report.getPrioritizedActions(),
                report.getFinalMarkdown()
        );
    }
}
