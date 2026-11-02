package com.cts.inventory;

import com.cts.inventory.model.Inventory;
import com.cts.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class InventoryJpaTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    @Rollback(value = false) // Ensures data stays permanently inside MySQL
    @DisplayName("Seed inventory quantities for all 30 catalog books")
    void seedInventoryForCatalogBooksTest() {

        // Match the 30 real book IDs generated in your Catalog Database
        long[] bookIds = {
                1L,2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L,
                11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L,
                21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L
        };

        Random random = new Random();
        List<Inventory> inventoryRecords = new ArrayList<>();

        for (long bookId : bookIds) {
            // Check to avoid primary/unique key constraint violations if rerun
            if (inventoryRepository.findByBookId(bookId).isPresent()) {
                System.out.println("Inventory entry already exists for bookId: " + bookId + ". Skipping.");
                continue;
            }

            // Generates a random number from 15 to 95 copies inclusive
            int randomQuantity = 15 + random.nextInt(81);

            Inventory inventory = Inventory.builder()
                    .bookId(bookId)
                    .quantity(randomQuantity)
                    .build();

            inventoryRecords.add(inventory);
        }

        // Batch save records to MySQL
        if (!inventoryRecords.isEmpty()) {
            inventoryRepository.saveAll(inventoryRecords);
        }

        long totalStockedItems = inventoryRepository.count();
        assertThat(totalStockedItems).isGreaterThanOrEqualTo(30);

        System.out.println("Inventory successfully verified. Total records: " + totalStockedItems);
    }
}