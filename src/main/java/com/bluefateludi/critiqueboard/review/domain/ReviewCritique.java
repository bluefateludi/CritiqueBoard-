package com.bluefateludi.critiqueboard.review.domain;

import com.bluefateludi.critiqueboard.review.agent.CritiqueResult;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "critique_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewCritique {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_task_id", nullable = false)
    private ReviewTask reviewTask;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_run_id", nullable = false)
    private AgentRun agentRun;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentRole role;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private String feedback;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> suggestions = new ArrayList<>();

    private double confidence;

    @OneToMany(mappedBy = "critique", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewCritiqueEvidence> evidence = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    private ReviewCritique(ReviewTask reviewTask, AgentRun agentRun, CritiqueResult result) {
        this.reviewTask = reviewTask;
        this.agentRun = agentRun;
        this.role = result.role();
        this.score = result.score();
        this.feedback = result.feedback();
        this.suggestions = new ArrayList<>(result.suggestions());
        this.confidence = result.confidence();
        this.createdAt = OffsetDateTime.now();
        this.evidence = new ArrayList<>(result.evidence().stream()
                .map(item -> ReviewCritiqueEvidence.from(this, item))
                .toList());
    }

    public static ReviewCritique from(ReviewTask reviewTask, AgentRun agentRun, CritiqueResult result) {
        return new ReviewCritique(reviewTask, agentRun, result);
    }
}
