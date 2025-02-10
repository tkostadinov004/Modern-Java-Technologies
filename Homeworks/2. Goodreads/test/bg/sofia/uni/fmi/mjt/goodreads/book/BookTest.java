package bg.sofia.uni.fmi.mjt.goodreads.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import java.util.List;

public class BookTest {
    @Test
    public void ofThrowsOnInputWithInvalidSize() {
        assertThrows(IllegalArgumentException.class,
                () -> Book.of(null),
                "Book.of should throw on null tokens array");
        assertThrows(IllegalArgumentException.class,
                () -> Book.of(new String[]{}),
                "Book.of should throw on empty tokens array");
        assertThrows(IllegalArgumentException.class,
                () -> Book.of(new String[]{"asdasd"}),
                "Book.of should throw on tokens array with length less than the amount of columns");
    }
    @Test
    public void ofCreatesBookCorrectly() {
        String[] input = {"100", "The Alchemist", "Paulo Coelho",
                "Sample description", "['Fantasy', 'Adventure']", "5.8", "123,456,789", "goodreads.com/something"};
        Book expected = new Book("100", "The Alchemist", "Paulo Coelho",
                "Sample description", List.of("Fantasy", "Adventure"), 5.8, 123456789, "goodreads.com/something");

        Book actual = Book.of(input);
        assertEquals(expected, actual,
                "Book created by its factory method should be equal to the same book created by the constructor");
    }
    @Test
    public void ofCreatesBookCorrectlyWithNoGenres() {
        String[] input = {"100", "The Alchemist", "Paulo Coelho",
                "Sample description", "[]", "5.8", "123,456,789", "goodreads.com/something"};
        Book expected = new Book("100", "The Alchemist", "Paulo Coelho",
                "Sample description", List.of(), 5.8, 123456789, "goodreads.com/something");

        Book actual = Book.of(input);
        assertEquals(expected, actual,
                "Book created by its factory method should be equal to the same book created by the constructor");
    }
}
