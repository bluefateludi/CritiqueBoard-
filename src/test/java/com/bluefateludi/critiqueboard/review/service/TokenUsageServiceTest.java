package com.bluefateludi.critiqueboard.review.service;

import com.bluefateludi.critiqueboard.review.domain.AgentRole;
import com.bluefateludi.critiqueboard.review.domain.AgentRun;
import com.bluefateludi.critiqueboard.review.domain.ReviewTask;
import com.bluefateludi.critiqueboard.review.domain.TokenUsageRecord;
import com.bluefateludi.critiqueboard.review.repository.AgentRunRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewTaskRepository;
import com.bluefateludi.critiqueboard.review.repository.TokenUsageRepository;
import dev.langchain4j.model.output.TokenUsage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TokenUsageServiceTest {

    @Test
    void recordsLangChainTokenUsageAndComputesEstimatedCost() {
        ReviewTaskRepository reviewTaskRepository = mock(ReviewTaskRepository.class);
        AgentRunRepository agentRunRepository = mock(AgentRunRepository.class);
        TokenUsageRepository tokenUsageRepository = mock(TokenUsageRepository.class);
        ReviewTask task = ReviewTask.create(
                "Launch Plan",
                "We will launch the product in Q3.",
                "Review structure, logic, and risk.",
                true
        );
        UUID reviewTaskId = UUID.randomUUID();
        UUID agentRunId = UUID.randomUUID();
        AgentRun run = AgentRun.create(task, AgentRole.STRUCTURE, 1, "Review structure.");
        when(reviewTaskRepository.findById(reviewTaskId)).thenReturn(Optional.of(task));
        when(agentRunRepository.findById(agentRunId)).thenReturn(Optional.of(run));
        when(tokenUsageRepository.save(any(TokenUsageRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TokenUsageService service = new TokenUsageService(reviewTaskRepository, agentRunRepository, tokenUsageRepository);

        service.recordModelUsage(
                reviewTaskId,
                agentRunId,
                "deepseek-chat",
                new TokenUsage(1_000, 500),
                new BigDecimal("0.14000000"),
                new BigDecimal("0.28000000")
        );

        ArgumentCaptor<TokenUsageRecord> captor = ArgumentCaptor.forClass(TokenUsageRecord.class);
        verify(tokenUsageRepository).save(captor.capture());
        TokenUsageRecord record = captor.getValue();
        assertThat(record.getAgentRun()).isSameAs(run);
        assertThat(record.getPromptTokens()).isEqualTo(1_000);
        assertThat(record.getCompletionTokens()).isEqualTo(500);
        assertThat(record.getTotalTokens()).isEqualTo(1_500);
        assertThat(record.getEstimatedCost()).isEqualByComparingTo("0.00028000");
    }
}
