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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "token_usage")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenUsageRecord {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_task_id", nullable = false)
    private ReviewTask reviewTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_run_id")
    private AgentRun agentRun;

    @Column(name = "model_name", nullable = false)
    private String modelName;

    @Column(name = "prompt_tokens", nullable = false)
    private int promptTokens;

    @Column(name = "completion_tokens", nullable = false)
    private int completionTokens;

    @Column(name = "total_tokens", nullable = false)
    private int totalTokens;

    @Column(name = "estimated_cost", nullable = false)
    private BigDecimal estimatedCost;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    private TokenUsageRecord(
            ReviewTask reviewTask,
            AgentRun agentRun,
            String modelName,
            int promptTokens,
            int completionTokens,
            BigDecimal estimatedCost
    ) {
        this.reviewTask = reviewTask;
        this.agentRun = agentRun;
        this.modelName = modelName;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = promptTokens + completionTokens;
        this.estimatedCost = estimatedCost;
        this.createdAt = OffsetDateTime.now();
    }

    public static TokenUsageRecord create(
            ReviewTask reviewTask,
            AgentRun agentRun,
            String modelName,
            int promptTokens,
            int completionTokens,
            BigDecimal estimatedCost
    ) {
        return new TokenUsageRecord(reviewTask, agentRun, modelName, promptTokens, completionTokens, estimatedCost);
    }
}
