package com.cts.review.dto;

import jakarta.validation.constraints.*;
import lombok.*;


/**
 * DTO representing the response returned after fetching review details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {

    /**
     * Unique identifier of the review.
     */
    @NotNull(message = "Review ID cannot be null")
    private Long reviewId;


    /**
     * Unique identifier of the user who submitted the review.
     */
    @NotNull(message = "User ID cannot be null")
    private Long userId;


    /**
     * Unique identifier of the book associated with the review.
     */
    @NotNull(message = "Book ID cannot be null")
    private Long bookId;


    /**
     * Rating given by the user.
     * Expected range: 1 to 5.
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
