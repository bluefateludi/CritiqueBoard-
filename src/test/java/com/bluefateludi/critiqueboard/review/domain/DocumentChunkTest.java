package com.bluefateludi.critiqueboard.review.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentChunkTest {

    @Test
    void createStoresTaskIndexContentAndEstimatedTokenCount() {
        ReviewTask task = ReviewTask.create(
                "Launch Plan",
                "First paragraph.",
                "Review structure, logic, and risk.",
                true
        );

        DocumentChunk chunk = DocumentChunk.create(task, 0, "First paragraph.");

        assertThat(chunk.getReviewTask()).isSameAs(task);
        assertThat(chunk.getChunkIndex()).isEqualTo(0);
        assertThat(chunk.getContent()).isEqualTo("First paragraph.");
        assertThat(chunk.getTokenCount()).isGreaterThan(0);
    }
}
