package com.bluefateludi.critiqueboard.review.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TokenUsageRecordTest {

    @Test
    void createsTokenUsageRecordForReviewTask() {
        ReviewTask task = ReviewTask.create(
                "Launch Plan",
                "We will launch the product in Q3.",
                "Review structure, logic, and risk.",
                true
        );

        TokenUsageRecord record = TokenUsageRecord.create(
                task,
                null,
                "deepseek-chat",
                100,
                40,
                new BigDecimal("0.00002000")
        );

        assertThat(record.getReviewTask()).isSameAs(task);
        assertThat(record.getAgentRun()).isNull();
        assertThat(record.getModelName()).isEqualTo("deepseek-chat");
        assertThat(record.getPromptTokens()).isEqualTo(100);
        assertThat(record.getCompletionTokens()).isEqualTo(40);
        assertThat(record.getTotalTokens()).isEqualTo(140);
        assertThat(record.getEstimatedCost()).isEqualByComparingTo("0.00002000");
    }
}
