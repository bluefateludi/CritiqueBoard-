package com.bluefateludi.critiqueboard.review.api;

import com.bluefateludi.critiqueboard.review.service.ReviewTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
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
}
