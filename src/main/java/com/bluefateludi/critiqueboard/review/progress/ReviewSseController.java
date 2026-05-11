package com.bluefateludi.critiqueboard.review.progress;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewSseController {

    private final ReviewEventService reviewEventService;

    public ReviewSseController(ReviewEventService reviewEventService) {
        this.reviewEventService = reviewEventService;
    }

    @GetMapping("/{id}/events")
    public SseEmitter subscribe(@PathVariable("id") UUID reviewTaskId) {
        return reviewEventService.subscribe(reviewTaskId);
    }
}
