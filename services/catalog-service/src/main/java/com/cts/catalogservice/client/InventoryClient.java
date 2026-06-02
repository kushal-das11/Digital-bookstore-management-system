package com.cts.catalogservice.client;

import com.cts.catalogservice.dto.response.AvailabilityDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * Feign client interface for communicating with the Inventory Service.
 *
 * <p>This client is used to fetch inventory-related information such as
 * availability of a book from the external inventory-service.</p>
 *
 * <p>Spring Cloud OpenFeign automatically generates the implementation
 * at runtime based on this interface.</p>
 */
@FeignClient(name = "inventory-service")
public interface InventoryClient {

/**
 * Retrieves the availability details of a specific book from the inventory service.
 *
 * @param bookId the unique identifier of the book whose availability is to be checked
 * @return {@link AvailabilityDto} containing availability status and related details
*/
    @GetMapping("/api/inventory/{bookId}/availability")
    AvailabilityDto checkAvailability(@PathVariable("bookId") Long bookId);
}
