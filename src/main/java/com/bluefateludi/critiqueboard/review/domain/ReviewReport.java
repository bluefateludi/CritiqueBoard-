package com.bluefateludi.critiqueboard.review.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "review_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewReport {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_task_id", nullable = false, unique = true)
    private ReviewTask reviewTask;

    @Column(name = "overall_score")
    private int overallScore;

    @Column(name = "executive_summary", nullable = false)
    private String executiveSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> strengths = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> weaknesses = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "prioritized_actions", nullable = false, columnDefinition = "jsonb")
    private List<String> prioritizedActions = new ArrayList<>();

    @Column(name = "second_round_performed", nullable = false)
    private boolean secondRoundPerformed;

    @Column(name = "final_markdown", nullable = false)
    private String finalMarkdown;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "report_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> reportJson = Map.of();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    private ReviewReport(
            ReviewTask reviewTask,
            int overallScore,
            String executiveSummary,
            List<String> strengths,
            List<String> weaknesses,
            List<String> prioritizedActions,
            String finalMarkdown,
            Map<String, Object> reportJson
    ) {
        this.reviewTask = reviewTask;
        this.overallScore = overallScore;
        this.executiveSummary = executiveSummary;
        this.strengths = new ArrayList<>(strengths);
        this.weaknesses = new ArrayList<>(weaknesses);
        this.prioritizedActions = new ArrayList<>(prioritizedActions);
        this.secondRoundPerformed = false;
        this.finalMarkdown = finalMarkdown;
        this.reportJson = Map.copyOf(reportJson);
        this.createdAt = OffsetDateTime.now();
    }

    public static ReviewReport create(
            ReviewTask reviewTask,
            int overallScore,
            String executiveSummary,
            List<String> strengths,
            List<String> weaknesses,
            List<String> prioritizedActions,
            String finalMarkdown,
            Map<String, Object> reportJson
    ) {
        return new ReviewReport(
                reviewTask,
                overallScore,
                executiveSummary,
                strengths,
                weaknesses,
                prioritizedActions,
                finalMarkdown,
                reportJson
        );
    }
}
