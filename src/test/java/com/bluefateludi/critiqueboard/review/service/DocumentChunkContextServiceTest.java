package com.bluefateludi.critiqueboard.review.service;

import com.bluefateludi.critiqueboard.review.domain.DocumentChunk;
import com.bluefateludi.critiqueboard.review.domain.ReviewTask;
import com.bluefateludi.critiqueboard.review.repository.DocumentChunkRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentChunkContextServiceTest {

    @Test
    void loadsChunksForReviewTaskAndMapsThemToSpecialistContext() {
        UUID reviewTaskId = UUID.randomUUID();
        UUID firstChunkId = UUID.randomUUID();
        UUID secondChunkId = UUID.randomUUID();
        DocumentChunkRepository repository = mock(DocumentChunkRepository.class);
        DocumentChunk firstChunk = chunk(firstChunkId, 0, "Executive summary.");
        DocumentChunk secondChunk = chunk(secondChunkId, 1, "Risk mitigation plan.");
        when(repository.findByReviewTaskIdOrderByChunkIndexAsc(reviewTaskId))
                .thenReturn(List.of(firstChunk, secondChunk));

        DocumentChunkContextService service = new DocumentChunkContextService(repository);

        var context = service.getContextForReviewTask(reviewTaskId);

        assertThat(context).hasSize(2);
        assertThat(context.get(0).documentChunkId()).isEqualTo(firstChunkId);
        assertThat(context.get(0).chunkIndex()).isEqualTo(0);
        assertThat(context.get(0).content()).isEqualTo("Executive summary.");
        assertThat(context.get(1).documentChunkId()).isEqualTo(secondChunkId);
        assertThat(context.get(1).chunkIndex()).isEqualTo(1);
        assertThat(context.get(1).content()).isEqualTo("Risk mitigation plan.");
        verify(repository).findByReviewTaskIdOrderByChunkIndexAsc(reviewTaskId);
    }

    private DocumentChunk chunk(UUID id, int chunkIndex, String content) {
        ReviewTask task = ReviewTask.create("Launch Plan", content, "Review requirement.", true);
        DocumentChunk chunk = DocumentChunk.create(task, chunkIndex, content);
        ReflectionTestUtils.setField(chunk, "id", id);
        return chunk;
    }
}
