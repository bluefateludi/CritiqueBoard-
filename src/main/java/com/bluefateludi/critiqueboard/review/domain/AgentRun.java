package com.bluefateludi.critiqueboard.review.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "agent_run")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgentRun {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_task_id", nullable = false)
    private ReviewTask reviewTask;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentRole role;

    @Column(name = "round_no", nullable = false)
    private int roundNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentRunStatus status;

    @Column(name = "input_summary")
    private String inputSummary;

    @Column(name = "output_summary")
    private String outputSummary;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    private AgentRun(ReviewTask reviewTask, AgentRole role, int roundNo, String inputSummary) {
        this.reviewTask = reviewTask;
        this.role = role;
        this.roundNo = roundNo;
        this.inputSummary = inputSummary;
        this.status = AgentRunStatus.PENDING;
        this.createdAt = OffsetDateTime.now();
    }

    public static AgentRun create(ReviewTask reviewTask, AgentRole role, int roundNo, String inputSummary) {
        return new AgentRun(reviewTask, role, roundNo, inputSummary);
    }

    public void markRunning() {
        status = AgentRunStatus.RUNNING;
        startedAt = OffsetDateTime.now();
    }

    public void markCompleted(String outputSummary) {
        this.outputSummary = outputSummary;
        status = AgentRunStatus.COMPLETED;
        finishedAt = OffsetDateTime.now();
    }

    public void markFailed(String errorMessage) {
        this.errorMessage = errorMessage;
        status = AgentRunStatus.FAILED;
        finishedAt = OffsetDateTime.now();
    }
}
