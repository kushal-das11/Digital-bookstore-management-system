
package com.cts.orderservice.client;

import com.cts.orderservice.dto.response.BookResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

/**
 * Feign client for catalog-service.
 */
@FeignClient(name = "catalog-service")
public interface CatalogClient {

    @GetMapping("/api/catalog/books/{bookId}")
    BookResponse getBookById(
            @PathVariable("bookId") Long bookId);

    @GetMapping("/api/catalog/books/search")
    List<BookResponse> searchBook(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category);
}
