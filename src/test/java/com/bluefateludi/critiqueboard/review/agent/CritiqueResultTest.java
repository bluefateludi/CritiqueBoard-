package com.bluefateludi.critiqueboard.review.agent;

import com.bluefateludi.critiqueboard.review.domain.AgentRole;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CritiqueResultTest {

    @Test
    void storesStructuredSpecialistFeedback() {
        UUID chunkId = UUID.randomUUID();
        CritiqueResult result = new CritiqueResult(
                AgentRole.STRUCTURE,
                82,
                "The document has clear sections but weak transitions.",
                List.of(new CritiqueResult.Evidence(
                        chunkId,
                        "The implementation plan begins before goals are defined.",
                        "This weakens the reader's understanding of sequence."
                )),
                List.of("Move goals before implementation details."),
                0.86
        );

        assertThat(result.role()).isEqualTo(AgentRole.STRUCTURE);
        assertThat(result.score()).isEqualTo(82);
        assertThat(result.feedback()).contains("weak transitions");
        assertThat(result.evidence()).singleElement()
                .extracting(CritiqueResult.Evidence::documentChunkId)
                .isEqualTo(chunkId);
        assertThat(result.suggestions()).containsExactly("Move goals before implementation details.");
        assertThat(result.confidence()).isEqualTo(0.86);
    }
}
