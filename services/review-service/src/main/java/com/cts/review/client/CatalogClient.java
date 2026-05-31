package com.cts.review.client;

import com.cts.review.dto.BookResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// name must match spring.application.name in catalog-service
@FeignClient(name = "catalog-service")
public interface CatalogClient {

    @GetMapping("/api/catalog/books/{bookId}")
    BookResponse getBookById(@PathVariable("bookId") Long bookId);

    @GetMapping("/api/catalog/books/search")
    List<BookResponse> searchBook(@RequestParam(required = false) String title,
                                  @RequestParam(required = false) String author,
                                  @RequestParam(required = false) String category);

}
