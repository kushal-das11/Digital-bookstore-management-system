package com.cts.catalogservice.controller;

import com.cts.catalogservice.dto.request.AuthorRequest;
import com.cts.catalogservice.dto.response.AuthorResponse;
import com.cts.catalogservice.service.AuthorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    public ResponseEntity<List<AuthorResponse>> listAuthors() {
        return ResponseEntity.ok(authorService.listAuthors());
    }

    @PostMapping
    public ResponseEntity<AuthorResponse> addAuthor(@Valid @RequestBody AuthorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authorService.addAuthor(request));
    }
}