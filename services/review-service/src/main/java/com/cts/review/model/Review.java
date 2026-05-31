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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "comment")
    private String comment;

    @Column(name = "edited_by_admin", nullable = false, columnDefinition = "tinyint(1) default 0")
    private boolean editedByAdmin;

}
