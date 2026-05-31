package com.cts.review.controller;

import com.cts.review.dto.*;
import com.cts.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> addReview(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId,
            @RequestBody ReviewRequestDTO request) {
        request.setUserId(Long.valueOf(userId));
        return ResponseEntity.ok(service.addReview(request));
    }

    @PutMapping("/{reviewId}/edit")
    public ResponseEntity<ReviewResponseDTO> editReview(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long reviewId,
            @RequestBody ReviewRequestDTO request) {
        return ResponseEntity.ok(service.editReview(reviewId, Long.valueOf(userId), request));
    }

    @PutMapping("/{reviewId}/moderate")
    public ResponseEntity<ReviewResponseDTO> moderateReview(
            @PathVariable Long reviewId,
            @RequestParam String comment) {
        return ResponseEntity.ok(service.moderateReview(reviewId, comment));
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<ReviewResponseDTO>> getByBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(service.getReviewsByBookId(bookId));
    }

    @GetMapping("/user")
    public ResponseEntity<List<ReviewResponseWithBookDetails>> getByUser(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(service.getReviewsByUserId(Long.valueOf(userId)));
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponseWithBookDetails>> getAll() {
        return ResponseEntity.ok(service.getAllReviews());
    }
}