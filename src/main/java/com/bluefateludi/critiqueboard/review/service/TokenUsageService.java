package com.bluefateludi.critiqueboard.review.service;

import com.bluefateludi.critiqueboard.review.domain.ReviewTask;
import com.bluefateludi.critiqueboard.review.domain.TokenUsageRecord;
import com.bluefateludi.critiqueboard.review.repository.AgentRunRepository;
import com.bluefateludi.critiqueboard.review.repository.ReviewTaskRepository;
import com.bluefateludi.critiqueboard.review.repository.TokenUsageRepository;
import dev.langchain4j.model.output.TokenUsage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class TokenUsageService {

    private static final BigDecimal ONE_MILLION = new BigDecimal("1000000");

    private final ReviewTaskRepository reviewTaskRepository;
    private final AgentRunRepository agentRunRepository;
    private final TokenUsageRepository tokenUsageRepository;

    public TokenUsageService(
            ReviewTaskRepository reviewTaskRepository,
            AgentRunRepository agentRunRepository,
            TokenUsageRepository tokenUsageRepository
    ) {
        this.reviewTaskRepository = reviewTaskRepository;
        this.agentRunRepository = agentRunRepository;
        this.tokenUsageRepository = tokenUsageRepository;
    }

    @Transactional
    public TokenUsageRecord recordModelUsage(
            UUID reviewTaskId,
            UUID agentRunId,
            String modelName,
            TokenUsage tokenUsage,
            BigDecimal inputCostPerMillionTokens,
            BigDecimal outputCostPerMillionTokens
    ) {
        int promptTokens = valueOrZero(tokenUsage.inputTokenCount());
        int completionTokens = valueOrZero(tokenUsage.outputTokenCount());
        BigDecimal estimatedCost = estimateCost(
                promptTokens,
                completionTokens,
                inputCostPerMillionTokens,
                outputCostPerMillionTokens
        );
        ReviewTask task = reviewTaskRepository.findById(reviewTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Review task not found: " + reviewTaskId));
        var run = agentRunId == null ? null : agentRunRepository.findById(agentRunId)
                .orElseThrow(() -> new IllegalArgumentException("Agent run not found: " + agentRunId));
        TokenUsageRecord record = TokenUsageRecord.create(
                task,
                run,
                modelName,
                promptTokens,
                completionTokens,
                estimatedCost
        );
        return tokenUsageRepository.save(record);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal estimateCost(
            int promptTokens,
            int completionTokens,
            BigDecimal inputCostPerMillionTokens,
            BigDecimal outputCostPerMillionTokens
    ) {
        BigDecimal inputCost = BigDecimal.valueOf(promptTokens)
                .multiply(inputCostPerMillionTokens)
                .divide(ONE_MILLION, 8, RoundingMode.HALF_UP);
        BigDecimal outputCost = BigDecimal.valueOf(completionTokens)
                .multiply(outputCostPerMillionTokens)
                .divide(ONE_MILLION, 8, RoundingMode.HALF_UP);
        return inputCost.add(outputCost).setScale(8, RoundingMode.HALF_UP);
    }
}
