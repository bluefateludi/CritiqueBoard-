package com.bluefateludi.critiqueboard.review.domain;

import com.bluefateludi.critiqueboard.review.agent.CritiqueResult;
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
@Table(name = "critique_evidence")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewCritiqueEvidence {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "critique_result_id", nullable = false)
    private ReviewCritique critique;

    @Column(name = "document_chunk_id")
    private UUID documentChunkId;

    private String quote;

    private String reason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    private ReviewCritiqueEvidence(ReviewCritique critique, CritiqueResult.Evidence evidence) {
        this.critique = critique;
        this.documentChunkId = evidence.documentChunkId();
        this.quote = evidence.quote();
        this.reason = evidence.reason();
        this.createdAt = OffsetDateTime.now();
    }

    static ReviewCritiqueEvidence from(ReviewCritique critique, CritiqueResult.Evidence evidence) {
        return new ReviewCritiqueEvidence(critique, evidence);
    }
}
