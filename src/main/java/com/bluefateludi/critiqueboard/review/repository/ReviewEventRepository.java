package com.bluefateludi.critiqueboard.review.repository;

import com.bluefateludi.critiqueboard.review.domain.ReviewEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewEventRepository extends JpaRepository<ReviewEvent, UUID> {

    List<ReviewEvent> findByReviewTaskIdOrderByCreatedAtAsc(UUID reviewTaskId);
}
