package com.cts.review.repository;

import java.util.Optional;

import com.cts.review.model.Review;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBookId(Long bookId);

    List<Review> findByUserId(Long userId);

    Optional<Review> findByUserIdAndBookId(Long userId, Integer bookId);
}

