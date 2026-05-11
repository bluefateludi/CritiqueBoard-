package com.bluefateludi.critiqueboard.review.repository;

import com.bluefateludi.critiqueboard.review.domain.ReviewCritique;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewCritiqueRepository extends JpaRepository<ReviewCritique, UUID> {

    List<ReviewCritique> findByReviewTaskIdOrderByCreatedAtAsc(UUID reviewTaskId);
}
