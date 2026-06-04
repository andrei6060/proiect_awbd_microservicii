package com.clinic.clinic.review;


import com.clinic.clinic.Controller.ReviewController;
import com.clinic.clinic.Service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = standaloneSetup(new ReviewController(reviewService)).build();
    }

    @Test
    void addReview_shouldReturnOk() throws Exception {
        String json = """
                {
                  "doctorId": 1,
                  "review": "Very good doctor",
                  "anonymousReview": false
                }
                """;

        mockMvc.perform(post("/review/addReview")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk());

        verify(reviewService).addReview(any());
    }

    @Test
    void getOwnReviews_shouldReturnReviews() throws Exception {
        when(reviewService.getOwnReviews()).thenReturn(List.of());

        mockMvc.perform(get("/review/getOwnReviews"))
                .andExpect(status().isOk());

        verify(reviewService).getOwnReviews();
    }

    @Test
    void getAllReviews_shouldReturnReviews() throws Exception {
        when(reviewService.getAllReviews()).thenReturn(List.of());

        mockMvc.perform(get("/review/getAllReviews"))
                .andExpect(status().isOk());

        verify(reviewService).getAllReviews();
    }

    @Test
    void deleteReview_shouldReturnOk() throws Exception {
        String json = """
                {
                  "id": 1
                }
                """;

        mockMvc.perform(delete("/review/deleteReview")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk());

        verify(reviewService).deleteReview(any());
    }
}
