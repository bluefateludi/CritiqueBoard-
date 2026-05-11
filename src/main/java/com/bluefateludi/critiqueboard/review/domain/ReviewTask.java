package com.bluefateludi.critiqueboard.review.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "review_task")
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

    protected ReviewTask() {
    }

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

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getRequirement() {
        return requirement;
    }

    public ReviewTaskStatus getStatus() {
        return status;
    }

    public boolean isSecondRoundEnabled() {
        return secondRoundEnabled;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
