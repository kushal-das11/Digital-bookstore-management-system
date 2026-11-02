package com.cts.catalogservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Entity representing a Book in the catalog system.
 *
 * <p>Maps to the <b>books</b> table and contains details such as
 * title, price, and relationships to author and category.</p>
 */
@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {


    /**
     * Unique identifier for the book.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long bookId;


    /**
     * Title of the book.
     *
     * <ul>
     *     <li>Cannot be null</li>
     *     <li>Maximum length: 200 characters</li>
     * </ul>
     */
    @Column(nullable = false, length = 200)
    private String title;


    /**
     * Price of the book.
     *
     * <ul>
     *     <li>Cannot be null</li>
     *     <li>Supports precision up to 10 digits with 2 decimal places</li>
     * </ul>
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;


    /**
     * Associated author of the book.
     *
     * <p>Many books can be associated with one author.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;


    /**
     * Category to which the book belongs.
     *
     * <p>Many books can belong to one category.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
