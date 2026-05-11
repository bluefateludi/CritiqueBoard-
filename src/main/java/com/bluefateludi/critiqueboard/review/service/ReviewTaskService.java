package com.bluefateludi.critiqueboard.review.service;

import com.bluefateludi.critiqueboard.review.domain.ReviewTask;
import com.bluefateludi.critiqueboard.review.repository.ReviewTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
}
