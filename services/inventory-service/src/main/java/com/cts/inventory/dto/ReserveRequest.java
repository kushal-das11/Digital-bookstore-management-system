package com.cts.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Sent to inventory-service for reserve and reduce operations
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReserveRequest {
    private Long bookId;
    private int quantity;
}
