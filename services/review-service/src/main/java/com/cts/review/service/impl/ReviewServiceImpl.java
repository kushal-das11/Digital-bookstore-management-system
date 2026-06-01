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


/**
 * Service implementation for managing review operations.
*/
@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository repository;
    private final CatalogClient catalogClient;


    /**
     * Maps a {@link Review} entity to {@link ReviewResponseDTO}.
     *
     * @param review the review entity
     * @return mapped ReviewResponseDTO
     */
    private ReviewResponseDTO mapToDTO(Review review) {
        return new ReviewResponseDTO(
                review.getReviewId(),
                review.getUserId(),
                review.getBookId(),
                review.getRating(),
                review.getComment()
        );
    }

    /**
     * Maps a {@link Review} entity to {@link ReviewResponseWithBookDetails}.
     * Fetches book details using Catalog Service with resilience.
     *
     * @param review the review entity
     * @return detailed review response with book information, or null if fetch fails
     */
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

    /**
     * Converts {@link ReviewRequestDTO} to {@link Review} entity.
     *
     * @param dto the incoming review request
     * @return mapped Review entity
     */
    private Review mapToEntity(ReviewRequestDTO dto) {
        return Review.builder()
                .userId(dto.getUserId())
                .bookId(dto.getBookId())
                .rating(dto.getRating())
                .comment(dto.getComment())
                .editedByAdmin(false)
                .build();
    }


    /**
     * Adds a new review.
     *
     * @param request the review request
     * @return created review response
     * @throws InvalidReviewException if rating is invalid
     */
    @Override
    public ReviewResponseDTO addReview(ReviewRequestDTO request) {

        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new InvalidReviewException("Rating must be between 1 and 5");
        }

        Review review = mapToEntity(request);
        return mapToDTO(repository.save(review));
    }


    /**
     * Edits an existing review.
     *
     * @param reviewId the review ID
     * @param userId   the user ID attempting to edit
     * @param request  updated review data
     * @return updated review response
     * @throws ReviewNotFoundException if review does not exist
     * @throws InvalidReviewException  if user is unauthorized or input is invalid
     */
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


    /**
     * Moderates a review (admin operation).
     *
     * @param reviewId the review ID
     * @param comment  the updated comment by admin
     * @return moderated review response
     * @throws InvalidReviewException  if comment is empty
     * @throws ReviewNotFoundException if review not found
     */
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


    /**
     * Retrieves reviews by book ID.
     *
     * @param bookId the book ID
     * @return list of review responses
     * @throws ReviewNotFoundException if no reviews found
     */
    @Override
    public List<ReviewResponseDTO> getReviewsByBookId(Long bookId) {

        List<Review> reviews = repository.findByBookId(bookId);

        if (reviews.isEmpty()) {
            throw new ReviewNotFoundException("No reviews found for bookId: " + bookId);
        }

        return reviews.stream().map(this::mapToDTO).toList();
    }


    /**
     * Retrieves reviews by user ID along with book details.
     *
     * @param userId the user ID
     * @return list of detailed review responses
     * @throws ReviewNotFoundException if no reviews found
     */
    @Override
    public List<ReviewResponseWithBookDetails> getReviewsByUserId(Long userId) {

        List<Review> reviews = repository.findByUserId(userId);

        if (reviews.isEmpty()) {
            throw new ReviewNotFoundException("No reviews found for userId: " + userId);
        }

        return reviews.stream().map(this::mapToDetailedDto).toList();
    }


    /**
     * Retrieves all reviews along with book details.
     *
     * @return list of all reviews
     * @throws ReviewOperationException if no reviews exist
     */
    @Override
    public List<ReviewResponseWithBookDetails> getAllReviews() {

        List<Review> reviews = repository.findAll();

        if (reviews.isEmpty()) {
            throw new ReviewOperationException("No reviews available");
        }

        return reviews.stream().map(this::mapToDetailedDto).toList();
    }


    /**
     * Fetches book details from Catalog Service with resilience.
     * <p>
     * Uses Retry and TimeLimiter for fault tolerance.
     * </p>
     *
     * @param bookId the book ID
     * @return CompletableFuture containing book response
     */
    @Retry(name = "catalogService", fallbackMethod = "catalogFallback")
    @TimeLimiter(name = "catalogService")
    public CompletableFuture<BookResponse> getBookWithResilience(Long bookId) {
        return CompletableFuture.supplyAsync(() ->
                catalogClient.getBookById(bookId)
        );
    }


    /**
     * Fallback method when Catalog Service fails.
     *
     * @param bookId the book ID
     * @param ex     the exception that caused failure
     * @return throws CatalogServiceDownException
     */
    public CompletableFuture<BookResponse> catalogFallback(Long bookId, Throwable ex) {
        throw new CatalogServiceDownException("Catalog service down. Cant fetch book with id : " + bookId + " exception: " + ex.getMessage());
    }

}
