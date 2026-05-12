package com.bluefateludi.critiqueboard.review.service;

import com.bluefateludi.critiqueboard.review.agent.CritiqueResult;
import com.bluefateludi.critiqueboard.review.domain.AgentRole;
import com.bluefateludi.critiqueboard.review.domain.AgentRun;
import com.bluefateludi.critiqueboard.review.domain.ReviewCritique;
import com.bluefateludi.critiqueboard.review.domain.ReviewReport;
import com.bluefateludi.critiqueboard.review.domain.ReviewTask;
import com.bluefateludi.critiqueboard.review.repository.ReviewCritiqueRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewReportRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewTaskRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewReportServiceTest {

    @Test
    void generatesFinalReportFromSpecialistCritiques() {
        UUID reviewTaskId = UUID.randomUUID();
        ReviewTask task = ReviewTask.create(
                "Launch Plan",
                "We will launch the product in Q3.",
                "Review structure, logic, and risk.",
                true
        );
        ReviewTaskRepository taskRepository = mock(ReviewTaskRepository.class);
        ReviewCritiqueRepository critiqueRepository = mock(ReviewCritiqueRepository.class);
        ReviewReportRepository reportRepository = mock(ReviewReportRepository.class);
        when(taskRepository.findById(reviewTaskId)).thenReturn(Optional.of(task));
        when(critiqueRepository.findByReviewTaskIdOrderByCreatedAtAsc(reviewTaskId))
                .thenReturn(List.of(
                        critique(task, AgentRole.STRUCTURE, 80, "The structure is clear.", List.of("Move conclusion earlier.")),
                        critique(task, AgentRole.LOGIC, 70, "Assumptions need support.", List.of("Add decision criteria.")),
                        critique(task, AgentRole.RISK, 60, "Risk owners are missing.", List.of("Assign mitigation owners."))
                ));
        when(reportRepository.save(any(ReviewReport.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ReviewReportService service = new ReviewReportService(taskRepository, critiqueRepository, reportRepository);

        ReviewReport report = service.generateFinalReport(reviewTaskId);

        assertThat(report.getReviewTask()).isSameAs(task);
        assertThat(report.getOverallScore()).isEqualTo(70);
        assertThat(report.getExecutiveSummary()).contains("3 specialist reviews");
        assertThat(report.getStrengths()).contains("The structure is clear.");
        assertThat(report.getWeaknesses()).contains("Assumptions need support.", "Risk owners are missing.");
        assertThat(report.getPrioritizedActions()).containsExactly(
                "Move conclusion earlier.",
                "Add decision criteria.",
                "Assign mitigation owners."
        );
        assertThat(report.getFinalMarkdown())
                .contains("# Review Report")
                .contains("## Actions")
                .contains("Assign mitigation owners.");
        verify(reportRepository).save(report);
    }

    private ReviewCritique critique(
            ReviewTask task,
            AgentRole role,
            int score,
            String feedback,
            List<String> suggestions
    ) {
        AgentRun run = AgentRun.create(task, role, 1, role + " input");
        run.markRunning();
        return ReviewCritique.from(task, run, new CritiqueResult(role, score, feedback, List.of(), suggestions, 0.8));
    }
}
