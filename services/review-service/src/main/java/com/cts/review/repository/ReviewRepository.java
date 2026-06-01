package com.cts.review.repository;

import java.util.Optional;

import com.cts.review.model.Review;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Repository interface for managing {@link Review} entities.
 * <p>
 * This interface provides CRUD operations along with custom query methods
 * for accessing review data based on different criteria such as book ID
 * and user ID.
 * </p>
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Retrieves all reviews associated with a specific book.
     *
     * @param bookId the ID of the book
     * @return list of reviews for the given book
     */
    List<Review> findByBookId(Long bookId);


    /**
     * Retrieves all reviews submitted by a specific user.
     *
     * @param userId the ID of the user
     * @return list of reviews submitted by the user
     */
    List<Review> findByUserId(Long userId);


    /**
     * Retrieves a review based on user ID and book ID.
     * <p>
     * This is typically used to ensure that a user has only one review per book.
     * </p>
     *
     * @param userId the ID of the user
     * @param bookId the ID of the book
     * @return an Optional containing the review if found, otherwise empty
     */
    Optional<Review> findByUserIdAndBookId(Long userId, Integer bookId);
}

