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
 * Entity representing an Author in the catalog system.
 *
 * <p>This class maps to the <b>authors</b> table in the database
 * and stores information related to book authors.</p>
 */
@Entity
@Table(name = "authors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Author {


    /**
     * Unique identifier for the author.
     *
     * <p>Auto-generated using IDENTITY strategy.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_id")
    private Long authorId;


    /**
     * Name of the author.
     *
     * <ul>
     *     <li>Cannot be null</li>
     *     <li>Must be unique</li>
     *     <li>Maximum length: 150 characters</li>
     * </ul>
     */
    @Column(name = "author_name", nullable = false, unique = true, length = 150)
    private String authorName;
}
