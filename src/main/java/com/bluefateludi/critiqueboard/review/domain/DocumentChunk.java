package com.bluefateludi.critiqueboard.review.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_chunk")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentChunk {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_task_id", nullable = false)
    private ReviewTask reviewTask;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(nullable = false)
    private String content;

    @Column(name = "token_count")
    private int tokenCount;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    private DocumentChunk(ReviewTask reviewTask, int chunkIndex, String content) {
        this.reviewTask = reviewTask;
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.tokenCount = estimateTokens(content);
        this.createdAt = OffsetDateTime.now();
    }

    public static DocumentChunk create(ReviewTask reviewTask, int chunkIndex, String content) {
        return new DocumentChunk(reviewTask, chunkIndex, content);
    }

    private static int estimateTokens(String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(content.length() / 4.0));
    }
}
