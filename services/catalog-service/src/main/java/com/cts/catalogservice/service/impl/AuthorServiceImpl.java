package com.cts.catalogservice.service.impl;

import com.cts.catalogservice.dto.request.AuthorRequest;
import com.cts.catalogservice.dto.response.AuthorResponse;
import com.cts.catalogservice.model.Author;
import com.cts.catalogservice.repository.AuthorRepository;
import com.cts.catalogservice.service.AuthorService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    @Override
    @Transactional
    public AuthorResponse addAuthor(AuthorRequest request) {
        String name = request.getAuthorName().trim();
        return authorRepository.findByAuthorNameIgnoreCase(name)
                .map(this::toResponse)
                .orElseGet(() -> {
                    Author saved = authorRepository.save(Author.builder().authorName(name).build());
                    log.info("Created author id={} name={}", saved.getAuthorId(), saved.getAuthorName());
                    return toResponse(saved);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthorResponse> listAuthors() {
        return authorRepository.findAll().stream().map(this::toResponse).toList();
    }

    private AuthorResponse toResponse(Author a) {
        return AuthorResponse.builder()
                .authorId(a.getAuthorId())
                .authorName(a.getAuthorName())
                .build();
    }
}
