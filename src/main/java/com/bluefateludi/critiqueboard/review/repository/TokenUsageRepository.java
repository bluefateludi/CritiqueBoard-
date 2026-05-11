package com.bluefateludi.critiqueboard.review.repository;

import com.bluefateludi.critiqueboard.review.domain.TokenUsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TokenUsageRepository extends JpaRepository<TokenUsageRecord, UUID> {
}
