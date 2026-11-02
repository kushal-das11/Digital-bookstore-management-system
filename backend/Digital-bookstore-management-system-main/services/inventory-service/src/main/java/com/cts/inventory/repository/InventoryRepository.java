package com.cts.inventory.repository;

import com.cts.inventory.model.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/**
 * Repository for managing {@link Inventory} data.
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Finds inventory by book ID.
     *
     * @param bookId book identifier
     * @return inventory if found
     */
    Optional<Inventory> findByBookId(Long bookId);


    /**
     * Deletes inventory by book ID.
     *
     * @param bookId book identifier
     */
    void deleteByBookId(Long bookId);


    /**
     * Checks if inventory exists for a book.
     *
     * @param bookId book identifier
     * @return true if exists, otherwise false
     */
    boolean existsByBookId(Long bookId);


    /**
     * Fetches inventory with a pessimistic lock
     * to ensure safe concurrent updates.
     *
     * @param bookId book identifier
     * @return locked inventory record
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.bookId = :bookId")
    Optional<Inventory> findByBookIdForUpdate(Long bookId);
}

