package com.cts.review;

import com.cts.review.controller.ReviewController;
import com.cts.review.dto.*;
import com.cts.review.exception.GlobalExceptionHandler;
import com.cts.review.exception.customexception.ReviewNotFoundException;
import com.cts.review.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for ReviewController.
 *
 * This class verifies request handling, validation behavior,
 * and exception handling for all controller endpoints.
 */
@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService service;

    @InjectMocks
    private ReviewController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private ReviewRequestDTO validRequest;
    private ReviewResponseDTO response;

    /**
     * Initializes MockMvc and test data.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        objectMapper = new ObjectMapper();

        validRequest = new ReviewRequestDTO(null, 100L, 5, "Excellent");

        response = new ReviewResponseDTO(1L, 1L, 100L, 5, "Excellent");
    }

    /**
     * Tests successful creation of a review.
     */
    @Test
    void addReview_success() throws Exception {

        when(service.addReview(any())).thenReturn(response);

        mockMvc.perform(post("/api/reviews")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1));

        verify(service).addReview(any());
    }

    /**
     * Tests validation failure when rating is invalid.
     */
    @Test
    void addReview_invalidRating_returns400() throws Exception {

        validRequest.setRating(0);

        mockMvc.perform(post("/api/reviews")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(service, never()).addReview(any());
    }

    /**
     * Tests missing header scenario.
     */
    @Test
    void addReview_missingHeader_returns400() throws Exception {

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(service, never()).addReview(any());
    }

    /**
     * Tests successful update of a review.
     */
    @Test
    void editReview_success() throws Exception {

        when(service.editReview(eq(1L), eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/reviews/1/edit")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    /**
     * Tests successful moderation of a review.
     */
    @Test
    void moderateReview_success() throws Exception {

        when(service.moderateReview(1L, "Updated"))
                .thenReturn(response);

        mockMvc.perform(put("/api/reviews/1/moderate")
                        .param("comment", "Updated"))
                .andExpect(status().isOk());
    }

    /**
     * Tests fetching reviews by book.
     */
    @Test
    void getByBook_success() throws Exception {

        when(service.getReviewsByBookId(100L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/reviews/book/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewId").value(1));
    }

    /**
     * Tests fetching reviews by user.
     */
    @Test
    void getByUser_success() throws Exception {

        ReviewResponseWithBookDetails dto = new ReviewResponseWithBookDetails();
        dto.setUserId(1L);

        when(service.getReviewsByUserId(1L))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/reviews/user")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1));
    }

    /**
     * Tests service failure scenario.
     */
    @Test
    void addReview_serviceException_returns500() throws Exception {

        when(service.addReview(any()))
                .thenThrow(new RuntimeException("error"));

        mockMvc.perform(post("/api/reviews")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Tests handling of resource not found scenario.
     */
    @Test
    void getByBook_notFound_returns404() throws Exception {

        when(service.getReviewsByBookId(100L))
                .thenThrow(new ReviewNotFoundException("Not found"));

        mockMvc.perform(get("/api/reviews/book/100"))
                .andExpect(status().isNotFound());
    }
}