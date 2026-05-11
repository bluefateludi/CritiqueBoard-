package com.bluefateludi.critiqueboard.review.repository;

import com.bluefateludi.critiqueboard.review.domain.AgentRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AgentRunRepository extends JpaRepository<AgentRun, UUID> {
}
