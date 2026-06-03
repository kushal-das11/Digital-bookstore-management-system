package com.cts.orderservice.client;

import com.cts.orderservice.dto.request.ReserveRequest;
import com.cts.orderservice.dto.response.AvailabilityDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client for inventory-service.
 */
@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @GetMapping("/api/inventory/{bookId}/availability")
    AvailabilityDto checkAvailability(
            @PathVariable("bookId") Long bookId);

    @PutMapping("/api/inventory/reduce")
    void reduce(@RequestBody ReserveRequest req);

    @PutMapping("/api/inventory/release")
    void release(@RequestBody ReserveRequest req);
}