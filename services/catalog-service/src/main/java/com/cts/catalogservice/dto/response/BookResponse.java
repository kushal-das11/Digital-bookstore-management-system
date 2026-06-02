package com.cts.catalogservice.dto.response;

import java.math.BigDecimal;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {

    private Long bookId;
    private String title;
    private BigDecimal price;
    private String authorName;
    private String categoryName;
    private Integer stockQuantity;
}
