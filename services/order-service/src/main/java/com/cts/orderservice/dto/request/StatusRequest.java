package com.cts.orderservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for updating order status.
 */
@Data
public class StatusRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotBlank(message = "Status is required")
    private String status;
}