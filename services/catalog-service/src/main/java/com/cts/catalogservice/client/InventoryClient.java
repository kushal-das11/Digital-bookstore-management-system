package com.cts.catalogservice.client;

import com.cts.catalogservice.dto.response.AvailabilityDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @GetMapping("/api/inventory/{bookId}/availability")
    AvailabilityDto checkAvailability(@PathVariable("bookId") Long bookId);
}
