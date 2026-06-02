package com.cts.catalogservice.dto.response;

import lombok.Data;

/**
 * DTO representing the response payload for availability of a Book.
 *
 * <p>This object is used to transfer author data from the server
 * to the client.</p>
 *
 * <p>Contains basic details such as Book identifier, name & inStock(Stock status).</p>
 */
@Data
public class AvailabilityDto {
    private Long bookId;
    private int availableQuantity;
    private boolean inStock;
}
