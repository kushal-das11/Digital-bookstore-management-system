package com.cts.inventory.service.impl;

import com.cts.inventory.dto.AvailabilityDto;
import com.cts.inventory.dto.InventoryRequestDTO;
import com.cts.inventory.dto.InventoryResponseDTO;
import com.cts.inventory.exception.customexception.InventoryNotFoundException;
import com.cts.inventory.exception.customexception.InvalidInventoryException;
import com.cts.inventory.exception.customexception.OutOfStockException;
import com.cts.inventory.model.Inventory;
import com.cts.inventory.repository.InventoryRepository;
import com.cts.inventory.service.InventoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Implementation of {@link InventoryService} for handling inventory operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository repository;


    /**
     * Converts Inventory entity to response DTO.
     */
    private InventoryResponseDTO mapToResponseDTO(Inventory inventory) {
        return new InventoryResponseDTO(
                inventory.getInventoryId(),
                inventory.getBookId(),
                inventory.getQuantity()
        );
    }

    /**
     * Converts request DTO to Inventory entity.
     */
    private Inventory mapToEntity(InventoryRequestDTO dto) {
        return Inventory.builder()
                .bookId(dto.getBookId())
                .quantity(dto.getQuantity())
                .build();
    }


    /**
     * Adds new inventory after validation.
     *
     * @param request inventory details
     * @return inventory details with generated id by repository
     * @throws InvalidInventoryException if Inventory already exists
     */
    @Override
    public InventoryResponseDTO addInventory(InventoryRequestDTO request) {

        if (request.getQuantity() <= 0) {
            throw new InvalidInventoryException("Quantity must be greater than zero");
        }

        if (repository.existsByBookId(request.getBookId())) {
            throw new InvalidInventoryException("Inventory already exists for this book");
        }

        Inventory saved = repository.save(mapToEntity(request));
        return mapToResponseDTO(saved);
    }

    /**
     * Retrieves inventory details for a given book ID.
     *
     * @param bookId unique book identifier
     * @return inventory details
     * @throws InventoryNotFoundException if no inventory is found
     */
    @Override
    public InventoryResponseDTO getInventoryByBookId(long bookId) {

        Inventory inventory = repository.findByBookId(bookId)
                .orElseThrow(() ->
                        new InventoryNotFoundException("Inventory not found for BookId: " + bookId));

        return mapToResponseDTO(inventory);
    }


    /**
     * Retrieves all inventory records.
     *
     * @return list of all inventory details
     */
    @Override
    public List<InventoryResponseDTO> getAllInventory() {

        return repository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Updates the quantity of a book.
     *
     * @param bookId   book identifier
     * @param quantity new quantity value
     * @return updated inventory details
     * @throws InvalidInventoryException  if quantity is negative
     * @throws InventoryNotFoundException if inventory does not exist
     */
    @Override
    @Transactional
    public InventoryResponseDTO updateQuantity(long bookId, int quantity) {

        if (quantity < 0) {
            throw new InvalidInventoryException("Quantity cannot be negative");
        }

        Inventory inventory = repository.findByBookId(bookId)
                .orElseThrow(() ->
                        new InventoryNotFoundException("Inventory not found"));

        inventory.setQuantity(quantity);

        // save is okay here (explicit update use case)
        return mapToResponseDTO(repository.save(inventory));
    }

    /**
     * Deletes inventory for a given book ID.
     *
     * @param bookId book identifier
     * @throws InventoryNotFoundException if inventory does not exist
     */
    @Override
    @Transactional
    public void deleteInventory(long bookId) {

        if (!repository.existsByBookId(bookId)) {
            throw new InventoryNotFoundException("Inventory not found");
        }

        repository.deleteByBookId(bookId);
    }

    /**
     * Checks whether stock is below minimum level.
     *
     * @param bookId book identifier
     * @return true if stock is low, otherwise false
     */
    @Override
    public boolean isStockLow(long bookId) {
        return repository.findByBookId(bookId)
                .map(inv -> inv.getQuantity() < 1)
                .orElse(true);
    }

    /**
     * Retrieves stock availability details for a book.
     *
     * @param bookId book identifier
     * @return availability information (empty if not found)
     */
    @Override
    public AvailabilityDto getStockDetails(long bookId) {

        Optional<Inventory> optional = repository.findByBookId(bookId);

        if (optional.isEmpty()) {
            return new AvailabilityDto(); // empty response
        }

        Inventory inventory = optional.get();

        AvailabilityDto dto = new AvailabilityDto();
        dto.setBookId(inventory.getBookId());
        dto.setAvailableQuantity(inventory.getQuantity());
        dto.setInStock(inventory.getQuantity() > 0);

        return dto;
    }

    /**
     * Checks if requested quantity is unavailable in stock.
     *
     * @param bookId   book identifier
     * @param orderQty requested quantity
     * @return true if stock is insufficient, otherwise false
     */
    @Override
    public boolean isOutOfStock(long bookId, int orderQty) {
        return repository.findByBookId(bookId)
                .map(inv -> inv.getQuantity() < orderQty)
                .orElse(true);
    }

    /**
     * Reduces the stock quantity safely using locking.
     *
     * @param bookId   book identifier
     * @param quantity quantity to reduce
     * @throws InvalidInventoryException  if quantity is invalid
     * @throws InventoryNotFoundException if inventory not found
     * @throws OutOfStockException        if stock is insufficient
     */
    @Override
    @Transactional
    public void reduceStock(long bookId, int quantity) {

        if (quantity <= 0) {
            throw new InvalidInventoryException("Quantity must be greater than zero");
        }

        Inventory inventory = repository.findByBookIdForUpdate(bookId)
                .orElseThrow(() ->
                        new InventoryNotFoundException("Inventory not found"));

        log.info("Before reduce: bookId={}, quantity={}", bookId, inventory.getQuantity());

        if (inventory.getQuantity() < quantity) {
            throw new OutOfStockException("Insufficient stock");
        }

        // Actual deduction happens here
        inventory.setQuantity(inventory.getQuantity() - quantity);

        repository.save(inventory);

        log.info("After reduce: bookId={}, quantity={}", bookId, inventory.getQuantity());
    }


    /**
     * Adds stock back to inventory safely using locking.
     *
     * @param bookId   book identifier
     * @param quantity quantity to add
     * @throws InvalidInventoryException  if quantity is less than or equal to zero
     * @throws InventoryNotFoundException if inventory is not found
     */
    @Override
    @Transactional
    public void releaseStock(long bookId, int quantity) {

        if (quantity <= 0) {
            throw new InvalidInventoryException("Quantity must be greater than zero");
        }

        Inventory inventory = repository.findByBookIdForUpdate(bookId)
                .orElseThrow(() ->
                        new InventoryNotFoundException("Inventory not found"));

        log.info("Before release: bookId={}, quantity={}", bookId, inventory.getQuantity());

        // give stock back
        inventory.setQuantity(inventory.getQuantity() + quantity);

        repository.save(inventory);

        log.info("After release: bookId={}, quantity={}", bookId, inventory.getQuantity());
    }


    /**
     * Retrieves all out-of-stock books.
     *
     * @return list of inventories with zero quantity
     */
    @Override
    public List<InventoryResponseDTO> allOutOfStockBooks() {
        return repository.findAll()
                .stream()
                .filter(inv -> inv.getQuantity() == 0)
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
}
