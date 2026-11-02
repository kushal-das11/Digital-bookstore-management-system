package com.cts.catalogservice;

import com.cts.catalogservice.model.Author;
import com.cts.catalogservice.model.Book;
import com.cts.catalogservice.model.Category;
import com.cts.catalogservice.repository.AuthorRepository;
import com.cts.catalogservice.repository.BookRepository;
import com.cts.catalogservice.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CatalogRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("Insert realistic Authors, Categories, and Books")
    void insertRealisticDataTest() {

        // AUTHORS (5–6 well-known style names)
        List<String> authorNames = Arrays.asList(
                "R.K. Narayan",
                "Chetan Bhagat",
                "Arundhati Roy",
                "Paulo Coelho",
                "George Orwell",
                "J.K. Rowling"
        );

        List<Author> authors = new ArrayList<>();
        for (String name : authorNames) {
            Author author = new Author();
            author.setAuthorName(name);
            authors.add(author);
        }
        authors = authorRepository.saveAll(authors);

        // CATEGORIES (3–4 meaningful ones)
        List<String> categoryNames = Arrays.asList(
                "Fiction",
                "Philosophy",
                "Classic Literature",
                "Fantasy"
        );

        List<Category> categories = new ArrayList<>();
        for (String name : categoryNames) {
            Category category = new Category();
            category.setCategoryName(name);
            categories.add(category);
        }
        categories = categoryRepository.saveAll(categories);

        // REAL BOOK TITLES (30)
        List<String> bookTitles = Arrays.asList(
                "The Guide",
                "Malgudi Days",
                "Half Girlfriend",
                "Five Point Someone",
                "The 3 Mistakes of My Life",
                "The God of Small Things",
                "The Alchemist",
                "Brida",
                "Eleven Minutes",
                "1984",
                "Animal Farm",
                "Homage to Catalonia",
                "Harry Potter and the Sorcerer's Stone",
                "Harry Potter and the Chamber of Secrets",
                "Harry Potter and the Prisoner of Azkaban",
                "Harry Potter and the Goblet of Fire",
                "Harry Potter and the Order of the Phoenix",
                "Harry Potter and the Half-Blood Prince",
                "Harry Potter and the Deathly Hallows",
                "The Zahir",
                "The Valkyries",
                "Ignited Minds",
                "Wings of Fire",
                "Revolution 2020",
                "2 States",
                "Digital Fortress",
                "The Monk Who Sold His Ferrari",
                "Veronika Decides to Die",
                "The Witch of Portobello",
                "The Pilgrimage"
        );

        Random random = new Random();

        List<Book> books = new ArrayList<>();

        for (String title : bookTitles) {
            Book book = new Book();
            book.setTitle(title);

            // realistic price between 200 - 800
            BigDecimal price = BigDecimal.valueOf(200 + (random.nextInt(600)));
            book.setPrice(price);

            // assign random author & category
            book.setAuthor(authors.get(random.nextInt(authors.size())));
            book.setCategory(categories.get(random.nextInt(categories.size())));

            books.add(book);
        }

        books = bookRepository.saveAll(books);

        // Assertions
        assertThat(authorRepository.count()).isEqualTo(6);
        assertThat(categoryRepository.count()).isEqualTo(4);
        assertThat(bookRepository.count()).isEqualTo(30);

        System.out.println("Authors inserted: " + authorRepository.count());
        System.out.println("Categories inserted: " + categoryRepository.count());
        System.out.println("Books inserted: " + bookRepository.count());
    }
}