package com.bluefateludi.critiqueboard.review.repository;

import com.bluefateludi.critiqueboard.review.domain.ReviewTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewTaskRepository extends JpaRepository<ReviewTask, UUID> {
}
