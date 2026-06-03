package com.cts.orderservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO matching what catalog-service returns.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookResponse {
    private Long bookId;
    private String title;
    private BigDecimal price;
    private String authorName;
    private String categoryName;
}
