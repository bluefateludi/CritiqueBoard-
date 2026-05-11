package com.bluefateludi.critiqueboard.review.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "review_task")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewTask {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;

    @Column(name = "original_text", nullable = false)
    private String originalText;

    @Column(nullable = false)
    private String requirement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewTaskStatus status;

    @Column(name = "second_round_enabled", nullable = false)
    private boolean secondRoundEnabled;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    private ReviewTask(String title, String originalText, String requirement, boolean secondRoundEnabled) {
        this.title = title;
        this.originalText = originalText;
        this.requirement = requirement;
        this.secondRoundEnabled = secondRoundEnabled;
        this.status = ReviewTaskStatus.PENDING;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static ReviewTask create(String title, String originalText, String requirement, boolean secondRoundEnabled) {
        return new ReviewTask(title, originalText, requirement, secondRoundEnabled);
    }

    public void markRunning() {
        markStatus(ReviewTaskStatus.RUNNING);
    }

    public void markSupervising() {
        markStatus(ReviewTaskStatus.SUPERVISING);
    }

    public void markSpecialistsRunning() {
        markStatus(ReviewTaskStatus.SPECIALISTS_RUNNING);
    }

    public void markSummarizing() {
        markStatus(ReviewTaskStatus.SUMMARIZING);
    }

    public void markCompleted() {
        completedAt = OffsetDateTime.now();
        status = ReviewTaskStatus.COMPLETED;
        updatedAt = completedAt;
    }

    private void markStatus(ReviewTaskStatus nextStatus) {
        status = nextStatus;
        updatedAt = OffsetDateTime.now();
    }
}
