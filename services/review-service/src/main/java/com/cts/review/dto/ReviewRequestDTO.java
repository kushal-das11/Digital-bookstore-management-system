package com.cts.review.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDTO {

    private Long userId;
    private Long bookId;
    private Integer rating;
    private String comment;
}
