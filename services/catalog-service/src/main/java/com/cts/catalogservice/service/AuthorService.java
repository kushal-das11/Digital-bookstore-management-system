package com.cts.catalogservice.service;

import com.cts.catalogservice.dto.request.AuthorRequest;
import com.cts.catalogservice.dto.response.AuthorResponse;
import java.util.List;

/**
 * Service interface for managing author-related operations.
 *
 * <p>Defines business logic for creating and retrieving authors.</p>
 */
public interface AuthorService {


    /**
     * Adds a new author or returns an existing one
     * if the author already exists (case-insensitive match).
     *
     * @param request author creation request
     * @return created or existing {@link AuthorResponse}
     */
    AuthorResponse addAuthor(AuthorRequest request);


    /**
     * Retrieves all authors from the system.
     *
     * @return list of {@link AuthorResponse}
     */
    List<AuthorResponse> listAuthors();
}
