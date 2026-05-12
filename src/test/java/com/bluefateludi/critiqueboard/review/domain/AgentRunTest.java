package com.bluefateludi.critiqueboard.review.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentRunTest {

    @Test
    void newAgentRunStartsPendingAndCanComplete() {
        ReviewTask task = ReviewTask.create(
                "Launch Plan",
                "We will launch the product in Q3.",
                "Review structure, logic, and risk.",
                true
        );

        AgentRun run = AgentRun.create(task, AgentRole.STRUCTURE, 1, "Structure specialist task");

        assertThat(run.getReviewTask()).isSameAs(task);
        assertThat(run.getRole()).isEqualTo(AgentRole.STRUCTURE);
        assertThat(run.getRoundNo()).isEqualTo(1);
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.PENDING);
        assertThat(run.getInputSummary()).isEqualTo("Structure specialist task");

        run.markRunning();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.RUNNING);
        assertThat(run.getStartedAt()).isNotNull();

        run.markCompleted("Structure is clear overall.");
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.COMPLETED);
        assertThat(run.getOutputSummary()).isEqualTo("Structure is clear overall.");
        assertThat(run.getFinishedAt()).isNotNull();
    }

    @Test
    void markFailedStoresErrorMessageAndFinishTime() {
        ReviewTask task = ReviewTask.create(
                "Launch Plan",
                "We will launch the product in Q3.",
                "Review structure, logic, and risk.",
                true
        );
        AgentRun run = AgentRun.create(task, AgentRole.RISK, 1, "Review risks.");
        run.markRunning();

        run.markFailed("LLM unavailable");

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.FAILED);
        assertThat(run.getErrorMessage()).isEqualTo("LLM unavailable");
        assertThat(run.getFinishedAt()).isNotNull();
    }
}
