package com.bluefateludi.critiqueboard.review.api;

import com.bluefateludi.critiqueboard.review.api.dto.ReviewTaskSummary;
import com.bluefateludi.critiqueboard.review.api.dto.SpecialistReviewSummary;
import com.bluefateludi.critiqueboard.review.domain.AgentRole;
import com.bluefateludi.critiqueboard.review.domain.ReviewTaskStatus;
import com.bluefateludi.critiqueboard.review.service.ReviewTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewTaskService reviewTaskService;

    @Test
    void createReviewReturnsPendingTask() throws Exception {
        UUID reviewTaskId = UUID.randomUUID();
        when(reviewTaskService.createReview(
                "Launch Plan",
                "We will launch the product in Q3.",
                "Review structure, logic, and risk.",
                true
        )).thenReturn(reviewTaskId);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Launch Plan",
                                  "text": "We will launch the product in Q3.",
                                  "requirement": "Review structure, logic, and risk.",
                                  "secondRoundEnabled": true
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.reviewTaskId", is(reviewTaskId.toString())))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void getReviewReturnsTaskStatus() throws Exception {
        UUID reviewTaskId = UUID.randomUUID();
        when(reviewTaskService.getReview(reviewTaskId))
                .thenReturn(new ReviewTaskSummary(
                        reviewTaskId,
                        "Launch Plan",
                        ReviewTaskStatus.RUNNING,
                        List.of(new SpecialistReviewSummary(
                                AgentRole.STRUCTURE,
                                78,
                                "The document has a workable structure.",
                                List.of("Move the main conclusion earlier."),
                                0.7
                        ))
                ));

        mockMvc.perform(get("/api/reviews/{id}", reviewTaskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewTaskId", is(reviewTaskId.toString())))
                .andExpect(jsonPath("$.title", is("Launch Plan")))
                .andExpect(jsonPath("$.status", is("RUNNING")))
                .andExpect(jsonPath("$.specialistReviews[0].role", is("STRUCTURE")))
                .andExpect(jsonPath("$.specialistReviews[0].score", is(78)))
                .andExpect(jsonPath("$.specialistReviews[0].suggestions[0]", is("Move the main conclusion earlier.")));
    }
}
