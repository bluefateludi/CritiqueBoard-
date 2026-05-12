package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.domain.AgentRole;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeterministicSpecialistReviewerTest {

    @Test
    void returnsRoleSpecificCritiqueForFallbackExecution() {
        DeterministicSpecialistReviewer reviewer = new DeterministicSpecialistReviewer();

        CritiqueResult result = reviewer.review(new SpecialistReviewRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                AgentRole.RISK,
                1,
                "Review risks, missing mitigations, and failure modes.",
                java.util.List.of(new DocumentChunkContext(
                        UUID.randomUUID(),
                        0,
                        "The rollout needs a readiness checklist before public release."
                ))
        ));

        assertThat(result.role()).isEqualTo(AgentRole.RISK);
        assertThat(result.score()).isBetween(0, 100);
        assertThat(result.feedback()).containsIgnoringCase("risk");
        assertThat(result.evidence()).hasSize(1);
        assertThat(result.evidence().getFirst().quote()).contains("readiness checklist");
        assertThat(result.suggestions()).isNotEmpty();
        assertThat(result.confidence()).isBetween(0.0, 1.0);
    }
}
