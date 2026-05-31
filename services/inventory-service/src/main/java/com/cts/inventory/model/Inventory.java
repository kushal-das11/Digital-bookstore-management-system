package com.cts.inventory.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "INVENTORY")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryId;

    @Column(name = "book_id", nullable = false, unique = true)
    private Long bookId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}
