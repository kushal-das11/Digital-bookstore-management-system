package com.cts.catalogservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO representing the response payload for an Author.
 *
 * <p>This object is used to transfer author data from the server
 * to the client.</p>
 *
 * <p>Contains basic details such as author identifier and name.</p>
*/
 @Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorResponse {

    private Long authorId;
    private String authorName;
}
