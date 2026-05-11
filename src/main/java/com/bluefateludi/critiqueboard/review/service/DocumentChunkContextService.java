package com.bluefateludi.critiqueboard.review.service;

import com.bluefateludi.critiqueboard.review.agent.DocumentChunkContext;
import com.bluefateludi.critiqueboard.review.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentChunkContextService {

    private final DocumentChunkRepository documentChunkRepository;

    public DocumentChunkContextService(DocumentChunkRepository documentChunkRepository) {
        this.documentChunkRepository = documentChunkRepository;
    }

    @Transactional(readOnly = true)
    public List<DocumentChunkContext> getContextForReviewTask(UUID reviewTaskId) {
        return documentChunkRepository.findByReviewTaskIdOrderByChunkIndexAsc(reviewTaskId)
                .stream()
                .map(chunk -> new DocumentChunkContext(
                        chunk.getId(),
                        chunk.getChunkIndex(),
                        chunk.getContent()
                ))
                .toList();
    }
}
