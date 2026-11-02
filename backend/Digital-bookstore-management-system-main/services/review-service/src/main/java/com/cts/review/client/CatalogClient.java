package com.cts.review.client;

import com.cts.review.dto.BookResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


/**
 * Feign client for communicating with the catalog-service.
 */
@FeignClient(name = "catalog-service")
public interface CatalogClient {

    /**
     * Retrieves book details by book ID.
     *
     * @param bookId unique book identifier
     * @return book details
     */
    @GetMapping("/api/catalog/books/{bookId}")
    BookResponse getBookById(@PathVariable("bookId") Long bookId);


    /**
     * Searches books based on filters.
     *
     * @param title book title (optional)
     * @param author book author (optional)
     * @param category book category (optional)
     * @return list of matching books
     */
    @GetMapping("/api/catalog/books/search")
    List<BookResponse> searchBook(@RequestParam(required = false) String title,
                                  @RequestParam(required = false) String author,
                                  @RequestParam(required = false) String category);

}
