package com.cts.review.service;

import com.cts.review.dto.ReviewRequestDTO;
import com.cts.review.dto.ReviewResponseDTO;
import com.cts.review.dto.ReviewResponseWithBookDetails;

import java.util.List;


/**
 * Service interface for managing review operations.
 * <p>
 * This interface defines business logic related to creating,
 * updating, moderating, and retrieving reviews.
 * </p>
 */
public interface ReviewService {

    /**
     * Adds a new review for a book.
     *
     * @param request the review request containing user input details
     * @return the created review response
     */
    ReviewResponseDTO addReview(ReviewRequestDTO request);


    /**
     * Edits an existing review.
     *
     * @param reviewId the ID of the review to be updated
     * @param userId   the ID of the user performing the update
     * @param request  the updated review details
     * @return the updated review response
     */
    ReviewResponseDTO editReview(Long reviewId, Long userId, ReviewRequestDTO request);


    /**
     * Moderates a review (typically performed by an admin).
     *
     * @param reviewId the ID of the review to be moderated
     * @param comment  the moderation comment or updated content
     * @return the moderated review response
     */
    ReviewResponseDTO moderateReview(Long reviewId, String comment);

    /**
     * Retrieves all reviews associated with a specific book.
     *
     * @param bookId the ID of the book
     * @return list of reviews for the given book
     */
    List<ReviewResponseDTO> getReviewsByBookId(Long bookId);

    /**
     * Retrieves all reviews submitted by a specific user,
     * along with corresponding book details.
     *
     * @param userId the ID of the user
     * @return list of reviews enriched with book details
     */
    List<ReviewResponseWithBookDetails> getReviewsByUserId(Long userId);


    /**
     * Retrieves all reviews in the system along with book details.
     *
     * @return list of all reviews with book details
     */
    List<ReviewResponseWithBookDetails>  getAllReviews();
}