package com.cts.review.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    /**
     * Unique identifier for the review.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;


    /**
     * ID of the user who submitted the review.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;


    /**
     * ID of the book being reviewed.
     */
    @Column(name = "book_id", nullable = false)
    private Long bookId;


    /**
     * Rating given by the user.
     * Typically ranges between 1 and 5.
     */
    @Column(nullable = false)
    private Integer rating;


    /**
     * Optional comment provided by the user.
     */
    @Column(name = "comment")
    private String comment;


    /**
     * Indicates whether the review has been edited by an administrator.
     * Default value is false (0 in database).
     */
    @Column(name = "edited_by_admin", nullable = false, columnDefinition = "tinyint(1) default 0")
    private boolean editedByAdmin;

}
