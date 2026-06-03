package com.cts.orderservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO sent to inventory-service
 * for reserve and reduce operations.
 */
@Data
@AllArgsConstructor
public class ReserveRequest {
    private Long bookId;
    private int  quantity;
}
