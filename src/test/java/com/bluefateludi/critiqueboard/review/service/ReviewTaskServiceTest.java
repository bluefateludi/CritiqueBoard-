package com.bluefateludi.critiqueboard.review.service;

import com.bluefateludi.critiqueboard.review.domain.ReviewTask;
import com.bluefateludi.critiqueboard.review.repository.ReviewTaskRepository;
import org.junit.jupiter.api.Test;
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
        CapturingPublisher publisher = new CapturingPublisher();
        UUID persistedId = UUID.randomUUID();
        when(repository.save(any(ReviewTask.class))).thenAnswer(invocation -> {
            ReviewTask task = invocation.getArgument(0);
            ReflectionTestUtils.setField(task, "id", persistedId);
            return task;
        });

        ReviewTaskService service = new ReviewTaskService(repository, publisher);

        UUID reviewTaskId = service.createReview(
                "Launch Plan",
                "We will launch the product in Q3.",
                "Review structure, logic, and risk.",
                true
        );

        assertThat(reviewTaskId).isEqualTo(persistedId);
        assertThat(publisher.publishedIds).containsExactly(persistedId);
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

        ReviewTaskService service = new ReviewTaskService(repository, new CapturingPublisher());

        service.markRunning(reviewTaskId);

        assertThat(task.getStatus()).isEqualTo(com.bluefateludi.critiqueboard.review.domain.ReviewTaskStatus.RUNNING);
        verify(repository).findById(reviewTaskId);
    }

    private static class CapturingPublisher implements ReviewTaskPublisher {
        private final List<UUID> publishedIds = new ArrayList<>();

        @Override
        public void publishReviewTaskCreated(UUID reviewTaskId) {
            publishedIds.add(reviewTaskId);
        }
    }
}
