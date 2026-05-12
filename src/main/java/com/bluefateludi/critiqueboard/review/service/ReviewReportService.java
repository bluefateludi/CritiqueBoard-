package com.bluefateludi.critiqueboard.review.service;

import com.bluefateludi.critiqueboard.review.domain.ReviewCritique;
import com.bluefateludi.critiqueboard.review.domain.ReviewReport;
import com.bluefateludi.critiqueboard.review.domain.ReviewTask;
import com.bluefateludi.critiqueboard.review.repository.ReviewCritiqueRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewReportRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReviewReportService {

    private final ReviewTaskRepository reviewTaskRepository;
    private final ReviewCritiqueRepository reviewCritiqueRepository;
    private final ReviewReportRepository reviewReportRepository;

    public ReviewReportService(
            ReviewTaskRepository reviewTaskRepository,
            ReviewCritiqueRepository reviewCritiqueRepository,
            ReviewReportRepository reviewReportRepository
    ) {
        this.reviewTaskRepository = reviewTaskRepository;
        this.reviewCritiqueRepository = reviewCritiqueRepository;
        this.reviewReportRepository = reviewReportRepository;
    }

    @Transactional
    public ReviewReport generateFinalReport(UUID reviewTaskId) {
        ReviewTask task = reviewTaskRepository.findById(reviewTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Review task not found: " + reviewTaskId));
        List<ReviewCritique> critiques = reviewCritiqueRepository.findByReviewTaskIdOrderByCreatedAtAsc(reviewTaskId);
        int overallScore = averageScore(critiques);
        List<String> strengths = critiques.stream()
                .filter(critique -> critique.getScore() >= 75)
                .map(ReviewCritique::getFeedback)
                .toList();
        List<String> weaknesses = critiques.stream()
                .filter(critique -> critique.getScore() < 75)
                .map(ReviewCritique::getFeedback)
                .toList();
        List<String> actions = critiques.stream()
                .flatMap(critique -> critique.getSuggestions().stream())
                .distinct()
                .toList();
        String summary = "Synthesized " + critiques.size()
                + " specialist reviews with an overall score of " + overallScore + ".";
        ReviewReport report = ReviewReport.create(
                task,
                overallScore,
                summary,
                strengths,
                weaknesses,
                actions,
                markdown(overallScore, summary, strengths, weaknesses, actions),
                Map.of(
                        "overallScore", overallScore,
                        "summary", summary,
                        "strengths", strengths,
                        "weaknesses", weaknesses,
                        "actions", actions
                )
        );
        return reviewReportRepository.save(report);
    }

    private int averageScore(List<ReviewCritique> critiques) {
        if (critiques.isEmpty()) {
            return 0;
        }
        return (int) Math.round(critiques.stream()
                .mapToInt(ReviewCritique::getScore)
                .average()
                .orElse(0));
    }

    private String markdown(
            int overallScore,
            String summary,
            List<String> strengths,
            List<String> weaknesses,
            List<String> actions
    ) {
        return """
                # Review Report
                
                ## Overall Score
                %d
                
                ## Summary
                %s
                
                ## Strengths
                %s
                
                ## Weaknesses
                %s
                
                ## Actions
                %s
                """.formatted(
                overallScore,
                summary,
                bulletList(strengths),
                bulletList(weaknesses),
                bulletList(actions)
        ).stripTrailing();
    }

    private String bulletList(List<String> items) {
        if (items.isEmpty()) {
            return "- None";
        }
        return items.stream()
                .map(item -> "- " + item)
                .reduce((left, right) -> left + "\n" + right)
                .orElse("- None");
    }
}
