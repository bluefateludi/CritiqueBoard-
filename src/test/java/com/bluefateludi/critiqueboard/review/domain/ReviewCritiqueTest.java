package com.bluefateludi.critiqueboard.review.domain;

import com.bluefateludi.critiqueboard.review.agent.CritiqueResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewCritiqueTest {

    @Test
    void createsCritiqueFromStructuredAgentResult() {
        ReviewTask task = ReviewTask.create(
                "Launch Plan",
                "We will launch the product in Q3.",
                "Review structure, logic, and risk.",
                true
        );
        AgentRun run = AgentRun.create(task, AgentRole.RISK, 1, "Risk specialist task");
        UUID chunkId = UUID.randomUUID();
        CritiqueResult result = new CritiqueResult(
                AgentRole.RISK,
                72,
                "The biggest risk is an unvalidated launch timeline.",
                List.of(new CritiqueResult.Evidence(chunkId, "launch the product in Q3", "Timeline has no dependency evidence.")),
                List.of("Add a launch readiness checklist."),
                0.82
        );

        ReviewCritique critique = ReviewCritique.from(task, run, result);

        assertThat(critique.getReviewTask()).isSameAs(task);
        assertThat(critique.getAgentRun()).isSameAs(run);
        assertThat(critique.getRole()).isEqualTo(AgentRole.RISK);
        assertThat(critique.getScore()).isEqualTo(72);
        assertThat(critique.getFeedback()).contains("unvalidated launch timeline");
        assertThat(critique.getSuggestions()).containsExactly("Add a launch readiness checklist.");
        assertThat(critique.getConfidence()).isEqualTo(0.82);
        assertThat(critique.getEvidence()).hasSize(1);
        assertThat(critique.getEvidence().getFirst().getDocumentChunkId()).isEqualTo(chunkId);
    }
}
