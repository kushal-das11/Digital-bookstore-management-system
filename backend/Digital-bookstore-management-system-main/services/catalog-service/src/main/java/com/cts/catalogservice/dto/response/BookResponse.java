package com.cts.catalogservice.dto.response;

import java.math.BigDecimal;

import lombok.*;

/**
 * DTO representing the response payload for a Book.
 *
 * <p>This object is used to transfer author data from the server
 * to the client.</p>
 *
 * <p>Contains basic details such as</p>
 * <p> book identifier, title, price, author name, category name. quantity in stock</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {

    private Long bookId;
    private String title;
    private BigDecimal price;
    private String authorName;
    private String categoryName;
    private Integer stockQuantity;
}
