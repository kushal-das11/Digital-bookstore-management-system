package com.cts.review;

import com.cts.review.controller.ReviewController;
import com.cts.review.dto.*;
import com.cts.review.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit test class for {@link ReviewController}.
 * <p>
 * This class uses Spring's {@link WebMvcTest} to test only the controller layer
 * in isolation by mocking the {@link ReviewService}.
 * </p>
 * <p>
 * It validates HTTP request handling, response structure, and integration
 * between Controller and Service layers.
 * </p>
 */
@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService service;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Tests successful creation of a review.
     */
    @Test
    void addReview_success() throws Exception {

        ReviewRequestDTO request = new ReviewRequestDTO(null, 100L, 5, "Excellent");

        ReviewResponseDTO response =
                new ReviewResponseDTO(1L, 1L, 100L, 5, "Excellent");

        when(service.addReview(org.mockito.ArgumentMatchers.any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/reviews")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1L))
                .andExpect(jsonPath("$.rating").value(5));
    }

    /**
     * Tests editing a review successfully.
     */
    @Test
    void editReview_success() throws Exception {

        ReviewRequestDTO request = new ReviewRequestDTO(null, 100L, 4, "Updated");

        ReviewResponseDTO response =
                new ReviewResponseDTO(1L, 1L, 100L, 4, "Updated");

        when(service.editReview(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/reviews/1/edit")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4));
    }

    /**
     * Tests moderating a review.
     */
    @Test
    void moderateReview_success() throws Exception {

        ReviewResponseDTO response =
                new ReviewResponseDTO(1L, 1L, 100L, 5, "Admin modified");

        when(service.moderateReview(1L, "Admin modified"))
                .thenReturn(response);

        mockMvc.perform(put("/api/reviews/1/moderate")
                        .param("comment", "Admin modified"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("Admin modified"));
    }

    /**
     * Tests fetching reviews by book ID.
     */
    @Test
    void getByBook_success() throws Exception {

        List<ReviewResponseDTO> list = List.of(
                new ReviewResponseDTO(1L, 1L, 100L, 5, "Good")
        );

        when(service.getReviewsByBookId(100L)).thenReturn(list);

        mockMvc.perform(get("/api/reviews/book/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewId").value(1L));
    }

    /**
     * Tests fetching reviews by user ID.
     */
    @Test
    void getByUser_success() throws Exception {

        ReviewResponseWithBookDetails response = new ReviewResponseWithBookDetails();
        response.setUserId(1L);

        when(service.getReviewsByUserId(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/reviews/user")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1L));
    }

    /**
     * Tests fetching all reviews.
     */
    @Test
    void getAll_success() throws Exception {

        ReviewResponseWithBookDetails response = new ReviewResponseWithBookDetails();
        response.setUserId(1L);

        when(service.getAllReviews()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1L));
    }
}
