package com.cts.review.service;

import com.cts.review.dto.ReviewRequestDTO;
import com.cts.review.dto.ReviewResponseDTO;
import com.cts.review.dto.ReviewResponseWithBookDetails;

import java.util.List;

public interface ReviewService {

    ReviewResponseDTO addReview(ReviewRequestDTO request);

    ReviewResponseDTO editReview(Long reviewId, Long userId, ReviewRequestDTO request);

    ReviewResponseDTO moderateReview(Long reviewId, String comment);

    List<ReviewResponseDTO> getReviewsByBookId(Long bookId);

    List<ReviewResponseWithBookDetails> getReviewsByUserId(Long userId);

    List<ReviewResponseWithBookDetails>  getAllReviews();
}