package com.cts.catalogservice.service;

import com.cts.catalogservice.dto.request.BookRequest;
import com.cts.catalogservice.dto.response.BookResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {

    BookResponse addBook(BookRequest request);

    BookResponse updateBook(Long bookId, BookRequest request);

    void deleteBook(Long bookId);

    BookResponse getBook(Long bookId);

    List<BookResponse> listBooks(Integer page, Integer pageSize, String fieldName, Boolean isAscending);

    List<BookResponse> searchBooks(String title, String author, String category);
}
