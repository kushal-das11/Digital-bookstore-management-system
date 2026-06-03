package com.cts.orderservice.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Response DTO for cart operations.
 * Returned instead of Cart entity
 * to avoid exposing JPA internals.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    private Long cartId;
    private Long userId;
    private Long bookId;
    private String bookTitle;
    private int quantity;
    private LocalDateTime addedAt;
}


