package com.bluefateludi.critiqueboard.review.service;

import com.bluefateludi.critiqueboard.review.chunk.DocumentChunker;
import com.bluefateludi.critiqueboard.review.domain.DocumentChunk;
import com.bluefateludi.critiqueboard.review.domain.ReviewTask;
import com.bluefateludi.critiqueboard.review.repository.DocumentChunkRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewTaskRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewTaskServiceTest {

    @Test
    void createReviewPersistsTaskAndPublishesItsId() {
        ReviewTaskRepository repository = mock(ReviewTaskRepository.class);
        DocumentChunkRepository chunkRepository = mock(DocumentChunkRepository.class);
        CapturingPublisher publisher = new CapturingPublisher();
        UUID persistedId = UUID.randomUUID();
        when(repository.save(any(ReviewTask.class))).thenAnswer(invocation -> {
            ReviewTask task = invocation.getArgument(0);
            ReflectionTestUtils.setField(task, "id", persistedId);
            return task;
        });

        ReviewTaskService service = new ReviewTaskService(repository, chunkRepository, new DocumentChunker(), publisher);

        UUID reviewTaskId = service.createReview(
                "Launch Plan",
                "We will launch the product in Q3.",
                "Review structure, logic, and risk.",
                true
        );

        assertThat(reviewTaskId).isEqualTo(persistedId);
        assertThat(publisher.publishedIds).containsExactly(persistedId);
        verify(chunkRepository).saveAll(any());
    }

    @Test
    void markRunningLoadsTaskAndAppliesTransition() {
        ReviewTaskRepository repository = mock(ReviewTaskRepository.class);
        ReviewTask task = ReviewTask.create(
                "Launch Plan",
                "We will launch the product in Q3.",
                "Review structure, logic, and risk.",
                true
        );
        UUID reviewTaskId = UUID.randomUUID();
        when(repository.findById(reviewTaskId)).thenReturn(Optional.of(task));

        ReviewTaskService service = new ReviewTaskService(repository, mock(DocumentChunkRepository.class), new DocumentChunker(), new CapturingPublisher());

        service.markRunning(reviewTaskId);

        assertThat(task.getStatus()).isEqualTo(com.bluefateludi.critiqueboard.review.domain.ReviewTaskStatus.RUNNING);
        verify(repository).findById(reviewTaskId);
    }

    @Test
    void createReviewPersistsDocumentChunksBeforePublishingTask() {
        ReviewTaskRepository repository = mock(ReviewTaskRepository.class);
        DocumentChunkRepository chunkRepository = mock(DocumentChunkRepository.class);
        CapturingPublisher publisher = new CapturingPublisher();
        UUID persistedId = UUID.randomUUID();
        when(repository.save(any(ReviewTask.class))).thenAnswer(invocation -> {
            ReviewTask task = invocation.getArgument(0);
            ReflectionTestUtils.setField(task, "id", persistedId);
            return task;
        });

        ReviewTaskService service = new ReviewTaskService(repository, chunkRepository, new DocumentChunker(), publisher);

        service.createReview(
                "Launch Plan",
                "First paragraph.\n\nSecond paragraph.",
                "Review structure, logic, and risk.",
                true
        );

        ArgumentCaptor<Iterable<DocumentChunk>> chunksCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(chunkRepository).saveAll(chunksCaptor.capture());
        List<DocumentChunk> savedChunks = new ArrayList<>();
        chunksCaptor.getValue().forEach(savedChunks::add);

        assertThat(savedChunks).hasSize(2);
        assertThat(savedChunks.get(0).getChunkIndex()).isEqualTo(0);
        assertThat(savedChunks.get(0).getContent()).isEqualTo("First paragraph.");
        assertThat(savedChunks.get(1).getChunkIndex()).isEqualTo(1);
        assertThat(savedChunks.get(1).getContent()).isEqualTo("Second paragraph.");
        assertThat(publisher.publishedIds).containsExactly(persistedId);
    }

    private static class CapturingPublisher implements ReviewTaskPublisher {
        private final List<UUID> publishedIds = new ArrayList<>();

        @Override
        public void publishReviewTaskCreated(UUID reviewTaskId) {
            publishedIds.add(reviewTaskId);
        }
    }

}
