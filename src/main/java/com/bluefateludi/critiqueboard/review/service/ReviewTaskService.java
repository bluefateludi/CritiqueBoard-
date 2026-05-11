package com.bluefateludi.critiqueboard.review.service;

import com.bluefateludi.critiqueboard.review.api.dto.ReviewTaskSummary;
import com.bluefateludi.critiqueboard.review.domain.ReviewTask;
import com.bluefateludi.critiqueboard.review.repository.ReviewTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Consumer;

@Service
public class ReviewTaskService {

    private final ReviewTaskRepository reviewTaskRepository;
    private final ReviewTaskPublisher reviewTaskPublisher;

    public ReviewTaskService(ReviewTaskRepository reviewTaskRepository, ReviewTaskPublisher reviewTaskPublisher) {
        this.reviewTaskRepository = reviewTaskRepository;
        this.reviewTaskPublisher = reviewTaskPublisher;
    }

    @Transactional
    public UUID createReview(String title, String originalText, String requirement, boolean secondRoundEnabled) {
        ReviewTask task = ReviewTask.create(title, originalText, requirement, secondRoundEnabled);
        ReviewTask saved = reviewTaskRepository.save(task);
        reviewTaskPublisher.publishReviewTaskCreated(saved.getId());
        return saved.getId();
    }

    @Transactional(readOnly = true)
    public ReviewTaskSummary getReview(UUID reviewTaskId) {
        ReviewTask task = reviewTaskRepository.findById(reviewTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Review task not found: " + reviewTaskId));
        return new ReviewTaskSummary(task.getId(), task.getTitle(), task.getStatus());
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

    private void updateTask(UUID reviewTaskId, Consumer<ReviewTask> transition) {
        ReviewTask task = reviewTaskRepository.findById(reviewTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Review task not found: " + reviewTaskId));
        transition.accept(task);
    }
}
