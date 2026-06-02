package com.cts.catalogservice.dto.response;

import lombok.Data;

// Fields must match what inventory-service returns
@Data
public class AvailabilityDto {
    private Long bookId;
    private int availableQuantity;
    private boolean inStock;
}
