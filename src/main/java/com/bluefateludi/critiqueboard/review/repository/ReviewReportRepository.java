package com.bluefateludi.critiqueboard.review.repository;

import com.bluefateludi.critiqueboard.review.domain.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, UUID> {

    Optional<ReviewReport> findByReviewTaskId(UUID reviewTaskId);
}
