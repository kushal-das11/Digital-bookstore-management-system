package com.cts.orderservice.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for order operations.
 * Returned instead of Orders entity to avoid exposing JPA internals.
 */
@Data
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemResponse> items;

    /**
     * Nested DTO representing a single item within an order.
     */
    @Data
    public static class OrderItemResponse {
        private Long orderItemId;
        private Long bookId;
        private String bookTitle;
        private int quantity;
        private BigDecimal unitPrice;
    }
}
