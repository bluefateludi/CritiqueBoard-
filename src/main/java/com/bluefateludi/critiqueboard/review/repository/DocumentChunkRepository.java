package com.bluefateludi.critiqueboard.review.repository;

import com.bluefateludi.critiqueboard.review.domain.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {
}
