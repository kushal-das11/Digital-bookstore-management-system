package com.cts.review.controller;

import com.cts.review.dto.*;
import com.cts.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * REST Controller for managing reviews.
 * Provides endpoints to create, update, moderate and fetch reviews.
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;


    /**
     * Adds a new review for a book.
     *
     * @param userId  User ID passed via header
     * @param request Review request payload
     * @return created review response
     */
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> addReview(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId,
            @RequestBody @Valid ReviewRequestDTO request) {
        request.setUserId(Long.valueOf(userId));
        log.info("Received request to add review for userId={}, bookId={}", userId, request.getBookId());

        return ResponseEntity.ok(service.addReview(request));
    }


    /**
     * Edits an existing review.
     *
     * @param userId   User ID from header
     * @param reviewId ID of review to edit
     * @param request  Updated review details
     * @return updated review response
     */
    @PutMapping("/{reviewId}/edit")
    public ResponseEntity<ReviewResponseDTO> editReview(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long reviewId,
            @RequestBody @Valid ReviewRequestDTO request) {

        log.info("Received request to edit reviewId={} by userId={}", reviewId, userId);
        return ResponseEntity.ok(service.editReview(reviewId, Long.valueOf(userId), request));
    }


    /**
     * Moderates a review (admin operation).
     *
     * @param reviewId ID of the review
     * @param comment  moderation comment
     * @return moderated review response
     */
    @PutMapping("/{reviewId}/moderate")
    public ResponseEntity<ReviewResponseDTO> moderateReview(
            @PathVariable Long reviewId,
            @RequestParam String comment) {

        log.info("Received admin moderation request for reviewId={}", reviewId);
        return ResponseEntity.ok(service.moderateReview(reviewId, comment));
    }


    /**
     * Fetches all reviews for a specific book.
     *
     * @param bookId Book ID
     * @return list of reviews
     */
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<ReviewResponseDTO>> getByBook(@PathVariable Long bookId) {
        log.info("Fetching reviews for bookId={}", bookId);
        return ResponseEntity.ok(service.getReviewsByBookId(bookId));
    }


    /**
     * Fetches all reviews by a specific user.
     *
     * @param userId User ID from header
     * @return list of reviews with book details
     */
    @GetMapping("/user")
    public ResponseEntity<List<ReviewResponseWithBookDetails>> getByUser(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId) {
        log.info("Fetching reviews for userId={}", userId);
        return ResponseEntity.ok(service.getReviewsByUserId(Long.valueOf(userId)));
    }

    /**
     * Fetches all reviews in the system.
     *
     * @return list of all reviews with book details
     */
    @GetMapping
    public ResponseEntity<List<ReviewResponseWithBookDetails>> getAll() {
        log.info("Fetching all reviews");
        return ResponseEntity.ok(service.getAllReviews());
    }
}