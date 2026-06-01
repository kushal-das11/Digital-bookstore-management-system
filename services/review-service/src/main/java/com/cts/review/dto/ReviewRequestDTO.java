package com.cts.review.dto;

import jakarta.validation.constraints.*;
import lombok.*;


/**
 * DTO representing the request payload for creating or updating a review.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDTO {

    /**
     * Unique identifier of the user submitting the review.
     */
    @NotNull(message = "User ID cannot be null")
    private Long userId;


    /**
     * Unique identifier of the book being reviewed.
     */
    @NotNull(message = "Book ID cannot be null")
    private Long bookId;


    /**
     * Rating given to the book.
     * Must be between 1 and 5.
     */
    @NotNull(message = "Rating cannot be null")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;


    /**
     * Review comment provided by the user.
     */
    @NotBlank(message = "Comment cannot be empty")
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String comment;
}
