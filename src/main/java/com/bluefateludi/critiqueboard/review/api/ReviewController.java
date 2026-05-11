package com.bluefateludi.critiqueboard.review.api;

import com.bluefateludi.critiqueboard.review.api.dto.CreateReviewRequest;
import com.bluefateludi.critiqueboard.review.api.dto.CreateReviewResponse;
import com.bluefateludi.critiqueboard.review.domain.ReviewTaskStatus;
import com.bluefateludi.critiqueboard.review.service.ReviewTaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewTaskService reviewTaskService;

    public ReviewController(ReviewTaskService reviewTaskService) {
        this.reviewTaskService = reviewTaskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CreateReviewResponse createReview(@Valid @RequestBody CreateReviewRequest request) {
        UUID reviewTaskId = reviewTaskService.createReview(
                request.title(),
                request.text(),
                request.requirement(),
                request.secondRoundEnabledOrDefault()
        );
        return new CreateReviewResponse(reviewTaskId, ReviewTaskStatus.PENDING);
    }
}
