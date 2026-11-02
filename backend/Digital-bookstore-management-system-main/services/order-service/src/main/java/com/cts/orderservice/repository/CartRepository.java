package com.cts.orderservice.repository;

import com.cts.orderservice.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUserId(Long userId);
    void deleteByUserId(Long userId);
    Optional<Cart> findByUserIdAndBookId(Long userId, Long bookId);
}
