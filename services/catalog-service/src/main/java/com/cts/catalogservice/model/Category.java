package com.cts.catalogservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Entity representing a Category in the catalog system.
 *
 * <p>Maps to the <b>categories</b> table and classifies books
 * into different categories or genres.</p>
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {


    /**
     * Unique identifier for the category.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * Name of the category.
     *
     * <ul>
     *     <li>Cannot be null</li>
     *     <li>Must be unique</li>
     *     <li>Maximum length: 100 characters</li>
     * </ul>
     */
    @Column(name = "category_name", nullable = false, unique = true, length = 100)
    private String categoryName;
}
