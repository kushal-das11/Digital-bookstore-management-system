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
    public InventoryResponseDTO addInventory(InventoryRequestDTO request) {
        log.info("Adding inventory for bookId={}, quantity={}", request.getBookId(), request.getQuantity());

        if (request.getQuantity() <= 0) {
            log.error("Invalid quantity={} for bookId={}", request.getQuantity(), request.getBookId());
            throw new InvalidInventoryException("Quantity must be greater than zero");
        }

        if (repository.existsByBookId(request.getBookId())) {
            log.warn("Inventory already exists for bookId={}", request.getBookId());
            throw new InvalidInventoryException("Inventory already exists for this book");
        }

        Inventory saved = repository.save(mapToEntity(request));
        log.info("Inventory created successfully for bookId={}", saved.getBookId());

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
        log.info("Fetching inventory for bookId={}", bookId);

        Inventory inventory = repository.findByBookId(bookId)
                .orElseThrow(() -> {
                    log.error("Inventory not found for bookId={}", bookId);
                    return new InventoryNotFoundException("Inventory not found for BookId: " + bookId);
                });

        log.info("Inventory retrieved for bookId={}", bookId);
        return mapToResponseDTO(inventory);
    }



    /**
     * Retrieves all inventory records.
     *
     * @return list of all inventory details
     */
    @Override
    public List<InventoryResponseDTO> getAllInventory() {
        log.info("Fetching all inventory records");

        List<InventoryResponseDTO> list = repository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();

        log.info("Total inventory records found={}", list.size());
        return list;
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
        log.info("Updating quantity for bookId={} to {}", bookId, quantity);

        if (quantity < 0) {
            log.error("Invalid quantity={} for bookId={}", quantity, bookId);
            throw new InvalidInventoryException("Quantity cannot be negative");
        }

        Inventory inventory = repository.findByBookId(bookId)
                .orElseThrow(() -> {
                    log.error("Inventory not found for update bookId={}", bookId);
                    return new InventoryNotFoundException("Inventory not found");
                });

        inventory.setQuantity(quantity);

        Inventory updated = repository.save(inventory);
        log.info("Inventory updated successfully for bookId={}", bookId);

        return mapToResponseDTO(updated);
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
        log.info("Deleting inventory for bookId={}", bookId);

        if (!repository.existsByBookId(bookId)) {
            log.error("Inventory not found for deletion bookId={}", bookId);
            throw new InventoryNotFoundException("Inventory not found");
        }

        repository.deleteByBookId(bookId);
        log.info("Inventory deleted successfully for bookId={}", bookId);
    }

    /**
     * Checks whether stock is below minimum level.
     *
     * @param bookId book identifier
     * @return true if stock is low, otherwise false
     */
    @Override
    public boolean isStockLow(long bookId) {
        log.info("Checking if stock is low for bookId={}", bookId);

        boolean result = repository.findByBookId(bookId)
                .map(inv -> inv.getQuantity() < 1)
                .orElse(true);

        log.info("Stock low status for bookId={} is {}", bookId, result);
        return result;
    }

    /**
     * Retrieves stock availability details for a book.
     *
     * @param bookId book identifier
     * @return availability information (empty if not found)
     */

    @Override
    public AvailabilityDto getStockDetails(long bookId) {
        log.info("Fetching stock details for bookId={}", bookId);

        Optional<Inventory> optional = repository.findByBookId(bookId);

        if (optional.isEmpty()) {
            log.warn("No inventory found for bookId={}", bookId);
            return new AvailabilityDto();
        }

        Inventory inventory = optional.get();

        AvailabilityDto dto = new AvailabilityDto();
        dto.setBookId(inventory.getBookId());
        dto.setAvailableQuantity(inventory.getQuantity());
        dto.setInStock(inventory.getQuantity() > 0);

        log.info("Stock details retrieved for bookId={}", bookId);
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
        log.info("Checking out-of-stock for bookId={}, requestedQty={}", bookId, orderQty);

        boolean result = repository.findByBookId(bookId)
                .map(inv -> inv.getQuantity() < orderQty)
                .orElse(true);

        log.info("Out-of-stock status for bookId={} is {}", bookId, result);
        return result;
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
        log.info("Reducing stock for bookId={}, quantity={}", bookId, quantity);

        if (quantity <= 0) {
            log.error("Invalid quantity={} for reduction bookId={}", quantity, bookId);
            throw new InvalidInventoryException("Quantity must be greater than zero");
        }

        Inventory inventory = repository.findByBookIdForUpdate(bookId)
                .orElseThrow(() -> {
                    log.error("Inventory not found for reduction bookId={}", bookId);
                    return new InventoryNotFoundException("Inventory not found");
                });

        log.info("Before reduce: bookId={}, quantity={}", bookId, inventory.getQuantity());

        if (inventory.getQuantity() < quantity) {
            log.warn("Insufficient stock for bookId={}, available={}, requested={}",
                    bookId, inventory.getQuantity(), quantity);
            throw new OutOfStockException("Insufficient stock");
        }

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
        log.info("Releasing stock for bookId={}, quantity={}", bookId, quantity);

        if (quantity <= 0) {
            log.error("Invalid quantity={} for release bookId={}", quantity, bookId);
            throw new InvalidInventoryException("Quantity must be greater than zero");
        }

        Inventory inventory = repository.findByBookIdForUpdate(bookId)
                .orElseThrow(() -> {
                    log.error("Inventory not found for release bookId={}", bookId);
                    return new InventoryNotFoundException("Inventory not found");
                });

        log.info("Before release: bookId={}, quantity={}", bookId, inventory.getQuantity());

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
        log.info("Fetching all out-of-stock books");

        List<InventoryResponseDTO> list = repository.findAll()
                .stream()
                .filter(inv -> inv.getQuantity() == 0)
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        log.info("Total out-of-stock books found={}", list.size());
        return list;
    }

}
