package com.cts.orderservice.dto.response;

import lombok.Data;

/**
 * Response DTO matching what inventory-service returns
 * for availability check.
 */

@Data
public class AvailabilityDto {
    private Long bookId;
    private int availableQuantity;
    private boolean inStock;
}

