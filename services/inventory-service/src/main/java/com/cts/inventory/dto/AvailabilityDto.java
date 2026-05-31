package com.cts.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Fields must match what inventory-service returns
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityDto {
    private Long bookId;
    private int availableQuantity;
    private boolean inStock;

}

