package com.cts.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponseWithBookDetails {
    private Long reviewId;
    private Long userId;
    private BookResponse book;
    private Integer rating;
    private String comment;
}
