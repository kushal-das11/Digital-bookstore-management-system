package com.cts.catalogservice.service;

import com.cts.catalogservice.dto.request.AuthorRequest;
import com.cts.catalogservice.dto.response.AuthorResponse;
import java.util.List;

public interface AuthorService {

    AuthorResponse addAuthor(AuthorRequest request);

    List<AuthorResponse> listAuthors();
}
