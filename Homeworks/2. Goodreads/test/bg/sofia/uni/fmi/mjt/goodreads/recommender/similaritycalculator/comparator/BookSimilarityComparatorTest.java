package bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.comparator;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.SimilarityCalculator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BookSimilarityComparatorTest {
    @Test
    public void comparesCorrectly() {
        Book origin = new Book("100", "The Alchemist", "Paulo Coelho",
                "Combining magic, mysticism, wisdom, and wonder into an inspiring tale of self-discovery, The Alchemist has become a modern classic, selling millions of copies around the world and transforming the lives of countless readers across generations.Paulo Coelho's masterpiece tells the mystical story of Santiago, an Andalusian shepherd boy who yearns to travel in search of a worldly treasure. His quest will lead him to riches far differentвЂ”and far more satisfyingвЂ”than he ever imagined. Santiago's journey teaches us about the essential wisdom of listening to our hearts, recognizing opportunity and learning to read the omens strewn along life's path, and, most importantly, following our dreams.\n",
                List.of("Fiction", "Classics", "Fantasy", "Philosophy", "Novels", "Spirituality", "Self-Help"),
                3.9, 2792673, "goodreads.com/something");
        SimilarityCalculator calculator = mock();

        BookSimilarityComparator comparator = new BookSimilarityComparator(origin, calculator);
        Book book1 = new Book("", "", "", "",
                List.of("Fiction", "Classics", "Fantasy"), 0, 123, "");
        Book book2 = new Book("", "", "", "",
                List.of( "Classics", "Fantasy", "Philosophy", "Novels", "Action"), 0, 123, "");

        when(calculator.calculateSimilarity(book1, origin))
                .thenReturn(1.0);
        when(calculator.calculateSimilarity(book2, origin))
                .thenReturn(0.8);

        assertTrue(comparator.compare(book1, book2) > 0,
                "A book with a higher similarity score to origin should be greater than a book with a lower similarity score");
        assertTrue(comparator.compare(book2, book1) < 0,
                "A book with a lower similarity score to origin should be greater than a book with a higher similarity score");
        assertTrue(comparator.compare(book1, book1) == 0,
                "If a book is compared to itself, it should return that they're equal");
        assertTrue(comparator.compare(book2, book2) == 0,
                "If a book is compared to itself, it should return that they're equal");

    }
}
