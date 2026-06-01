package com.cts.review;

import com.cts.review.client.CatalogClient;
import com.cts.review.dto.*;
import com.cts.review.exception.customexception.InvalidReviewException;
import com.cts.review.exception.customexception.ReviewNotFoundException;
import com.cts.review.exception.customexception.ReviewOperationException;
import com.cts.review.model.Review;
import com.cts.review.repository.ReviewRepository;
import com.cts.review.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test class for {@link ReviewServiceImpl}.
 * <p>
 * This class validates all business logic of the Review Service layer,
 * including creation, editing, moderation, and retrieval of reviews.
 * </p>
 * <p>
 * Uses Mockito to mock dependencies such as {@link ReviewRepository}
 * and {@link CatalogClient}.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository repository;

    @Mock
    private CatalogClient catalogClient;

    @InjectMocks
    private ReviewServiceImpl service;

    /**
     * Tests successful review creation.
     */
    @Test
    void addReview_success() {
        ReviewRequestDTO request = new ReviewRequestDTO(1L, 100L, 5, "Great");

        Review saved = Review.builder()
                .reviewId(10L).userId(1L).bookId(100L)
                .rating(5).comment("Great").editedByAdmin(false)
                .build();

        when(repository.save(any())).thenReturn(saved);

        ReviewResponseDTO response = service.addReview(request);

        assertEquals(10L, response.getReviewId());
        verify(repository).save(any());
    }

    /**
     * Tests review creation failure due to invalid rating.
     */
    @Test
    void addReview_invalidRating() {
        ReviewRequestDTO request = new ReviewRequestDTO(1L, 100L, 6, "Bad");

        assertThrows(InvalidReviewException.class,
                () -> service.addReview(request));

        verify(repository, never()).save(any());
    }

    /**
     * Tests successful editing of a review.
     */
    @Test
    void editReview_success() {
        Review existing = Review.builder()
                .reviewId(1L).userId(1L).bookId(100L)
                .rating(3).comment("Old").editedByAdmin(false)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(existing);

        ReviewRequestDTO request = new ReviewRequestDTO(1L, 100L, 5, "Updated");

        ReviewResponseDTO response = service.editReview(1L, 1L, request);

        assertEquals(5, response.getRating());
        verify(repository).save(existing);
    }

    /**
     * Tests editing failure when the user is not the owner.
     */
    @Test
    void editReview_notOwner() {
        Review review = Review.builder()
                .reviewId(1L).userId(2L).editedByAdmin(false).build();

        when(repository.findById(1L)).thenReturn(Optional.of(review));

        assertThrows(InvalidReviewException.class,
                () -> service.editReview(1L, 1L, new ReviewRequestDTO()));
    }

    /**
     * Tests editing failure when review is not found.
     */
    @Test
    void editReview_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class,
                () -> service.editReview(1L, 1L, new ReviewRequestDTO()));
    }

    /**
     * Tests successful moderation of a review by admin.
     */
    @Test
    void moderateReview_success() {
        Review review = Review.builder()
                .reviewId(1L).comment("Old").build();

        when(repository.findById(1L)).thenReturn(Optional.of(review));
        when(repository.save(any())).thenReturn(review);

        ReviewResponseDTO response = service.moderateReview(1L, "Admin edit");

        assertTrue(review.isEditedByAdmin());
        assertTrue(review.getComment().contains("ADMIN_MODIFIED"));
    }

    /**
     * Tests moderation failure when comment is empty.
     */
    @Test
    void moderateReview_emptyComment() {
        assertThrows(InvalidReviewException.class,
                () -> service.moderateReview(1L, ""));
    }

    /**
     * Tests fetching reviews by book ID successfully.
     */
    @Test
    void getReviewsByBookId_success() {
        when(repository.findByBookId(100L))
                .thenReturn(List.of(new Review()));

        List<ReviewResponseDTO> result = service.getReviewsByBookId(100L);

        assertFalse(result.isEmpty());
    }

    /**
     * Tests fetching reviews by book ID when none exist.
     */
    @Test
    void getReviewsByBookId_empty() {
        when(repository.findByBookId(100L)).thenReturn(List.of());

        assertThrows(ReviewNotFoundException.class,
                () -> service.getReviewsByBookId(100L));
    }

    /**
     * Tests fetching reviews by user ID with book details.
     */
    @Test
    void getReviewsByUserId_success() {
        Review review = Review.builder()
                .reviewId(1L).userId(1L).bookId(100L)
                .rating(5).comment("Good").build();

        BookResponse book = new BookResponse();
        book.setBookId(100L);

        when(repository.findByUserId(1L)).thenReturn(List.of(review));
        when(catalogClient.getBookById(100L)).thenReturn(book);

        List<ReviewResponseWithBookDetails> result =
                service.getReviewsByUserId(1L);

        assertNotNull(result);
    }

    /**
     * Tests fetching reviews by user ID when none exist.
     */
    @Test
    void getReviewsByUserId_empty() {
        when(repository.findByUserId(1L)).thenReturn(List.of());

        assertThrows(ReviewNotFoundException.class,
                () -> service.getReviewsByUserId(1L));
    }

    /**
     * Tests fetching all reviews successfully.
     */
    @Test
    void getAllReviews_success() {
        Review review = new Review();
        review.setBookId(100L);

        when(repository.findAll()).thenReturn(List.of(review));
        when(catalogClient.getBookById(100L)).thenReturn(new BookResponse());

        List<ReviewResponseWithBookDetails> result = service.getAllReviews();

        assertFalse(result.isEmpty());
    }

    /**
     * Tests fetching all reviews when none exist.
     */
    @Test
    void getAllReviews_empty() {
        when(repository.findAll()).thenReturn(List.of());

        assertThrows(ReviewOperationException.class,
                () -> service.getAllReviews());
    }
}
