package com.cts.catalogservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO representing the response payload for a Category.
 *
 * <p>This object is used to transfer author data from the server
 * to the client.</p>
 *
 * <p>Contains basic details such as category identifier and name.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private Long categoryId;
    private String categoryName;
}
