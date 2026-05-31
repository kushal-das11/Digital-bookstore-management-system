package com.cts.review.service.impl;

import com.cts.review.client.CatalogClient;
import com.cts.review.dto.*;
import com.cts.review.exception.customexception.InvalidReviewException;
import com.cts.review.exception.customexception.ReviewNotFoundException;
import com.cts.review.exception.customexception.ReviewOperationException;
import com.cts.review.exception.feignclientexception.CatalogServiceDownException;
import com.cts.review.model.Review;
import com.cts.review.repository.ReviewRepository;
import com.cts.review.service.ReviewService;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository repository;
    private final CatalogClient catalogClient;

    private ReviewResponseDTO mapToDTO(Review review) {
        return new ReviewResponseDTO(
                review.getReviewId(),
                review.getUserId(),
                review.getBookId(),
                review.getRating(),
                review.getComment()
        );
    }
    private ReviewResponseWithBookDetails mapToDetailedDto(Review review) {
        try {
            BookResponse bookResponse =
                    getBookWithResilience(review.getBookId()).join();

            return new ReviewResponseWithBookDetails(
                    review.getReviewId(),
                    review.getUserId(),
                    bookResponse,
                    review.getRating(),
                    review.getComment()
            );

        } catch (Exception ex) {
            log.warn("Skipping reviewId={} due to book fetch failure",
                    review.getReviewId());

            return null; // mark for skipping
        }
    }

    private Review mapToEntity(ReviewRequestDTO dto) {
        return Review.builder()
                .userId(dto.getUserId())
                .bookId(dto.getBookId())
                .rating(dto.getRating())
                .comment(dto.getComment())
                .editedByAdmin(false)
                .build();
    }

    @Override
    public ReviewResponseDTO addReview(ReviewRequestDTO request) {

        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new InvalidReviewException("Rating must be between 1 and 5");
        }

        Review review = mapToEntity(request);
        return mapToDTO(repository.save(review));
    }

    @Override
    public ReviewResponseDTO editReview(Long reviewId, Long userId, ReviewRequestDTO request) {

        Review review = repository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found"));

        if (!review.getUserId().equals(userId)) {
            throw new InvalidReviewException("You can only edit your own review");
        }

        if (review.isEditedByAdmin()) {
            throw new InvalidReviewException("Review cannot be edited after admin moderation");
        }

        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new InvalidReviewException("Rating must be between 1 and 5");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return mapToDTO(repository.save(review));
    }

    @Override
    public ReviewResponseDTO moderateReview(Long reviewId, String comment) {

        if (comment == null || comment.trim().isEmpty()) {
            throw new InvalidReviewException("Comment cannot be empty");
        }

        Review review = repository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found"));

        review.setComment(comment + " [ADMIN_MODIFIED]");
        review.setEditedByAdmin(true);

        return mapToDTO(repository.save(review));
    }

    @Override
    public List<ReviewResponseDTO> getReviewsByBookId(Long bookId) {

        List<Review> reviews = repository.findByBookId(bookId);

        if (reviews.isEmpty()) {
            throw new ReviewNotFoundException("No reviews found for bookId: " + bookId);
        }

        return reviews.stream().map(this::mapToDTO).toList();
    }

    @Override
    public List<ReviewResponseWithBookDetails> getReviewsByUserId(Long userId) {

        List<Review> reviews = repository.findByUserId(userId);

        if (reviews.isEmpty()) {
            throw new ReviewNotFoundException("No reviews found for userId: " + userId);
        }

        return reviews.stream().map(this::mapToDetailedDto).toList();
    }

    @Override
    public List<ReviewResponseWithBookDetails> getAllReviews() {

        List<Review> reviews = repository.findAll();

        if (reviews.isEmpty()) {
            throw new ReviewOperationException("No reviews available");
        }

        return reviews.stream().map(this::mapToDetailedDto).toList();
    }

    @Retry(name = "catalogService", fallbackMethod = "catalogFallback")
    @TimeLimiter(name = "catalogService")
    public CompletableFuture<BookResponse> getBookWithResilience(Long bookId) {
        return CompletableFuture.supplyAsync(() ->
                catalogClient.getBookById(bookId)
        );
    }

    public CompletableFuture<BookResponse> catalogFallback(Long bookId, Throwable ex) {
        throw new CatalogServiceDownException("Catalog service down. Cant fetch book with id : " + bookId + " exception: " + ex.getMessage());
    }

}
