package bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.composite;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.SimilarityCalculator;
import bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.descriptions.TFIDFSimilarityCalculator;
import bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.genres.GenresOverlapSimilarityCalculator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompositeSimilarityTestCalculatorTest {
    private static Map<SimilarityCalculator, Double> weights;
    private static Book book1;
    private static Book book2;

    @BeforeAll
    public static void setUp() {
        weights = new LinkedHashMap<>();
        book1 = new Book("101", "The Hobbit", "J.R.R. Tolkien",
                "In a hole in the ground there lived a hobbit. Not a nasty, dirty, wet hole, filled with the ends of worms and an oozy smell, nor yet a dry, bare, sandy hole with nothing in it to sit down on or to eat: it was a hobbit-hole, and that means comfort.Written for J.R.R. TolkienвЂ™s own children, The Hobbit met with instant critical acclaim when it was first published in 1937. Now recognized as a timeless classic, this introduction to the hobbit Bilbo Baggins, the wizard Gandalf, Gollum, and the spectacular world of Middle-earth recounts of the adventures of a reluctant hero, a powerful and dangerous ring, and the cruel dragon Smaug the Magnificent. The text in this 372-page paperback edition is based on that first published in Great Britain by Collins Modern Classics (1998), and includes a note on the text by Douglas A. Anderson (2001).\n",
                List.of("Fantasy", "Classics", "Fiction", "Adventure", "Young Adult", "Science Fiction Fantasy", "High Fantasy"),
                4.33, 7963002, "goodreads.com/something1");
        book2 = new Book("104", "The Lord of the Rings", "J.R.R. Tolkien",
                "One Ring to rule them all, One Ring to find them, One Ring to bring them all and in the darkness bind themIn ancient times the Rings of Power were crafted by the Elven-smiths, and Sauron, the Dark Lord, forged the One Ring, filling it with his own power so that he could rule all others. But the One Ring was taken from him, and though he sought it throughout Middle-earth, it remained lost to him. After many ages it fell by chance into the hands of the hobbit Bilbo Baggins.From Sauron's fastness in the Dark Tower of Mordor, his power spread far and wide. Sauron gathered all the Great Rings to him, but always he searched for the One Ring that would complete his dominion.When Bilbo reached his eleventy-first birthday he disappeared, bequeathing to his young cousin Frodo the Ruling Ring and a perilous quest: to journey across Middle-earth, deep into the shadow of the Dark Lord, and destroy the Ring by casting it into the Cracks of Doom.The Lord of the Rings tells of the great quest undertaken by Frodo and the Fellowship of the Ring: Gandalf the Wizard; the hobbits Merry, Pippin, and Sam; Gimli the Dwarf; Legolas the Elf; Boromir of Gondor; and a tall, mysterious stranger called Strider.\n",
                List.of("Fantasy", "Classics", "Fiction", "Adventure", "Science Fiction Fantasy", "High Fantasy", "Epic Fantasy"),
                4.34, 722312,"goodreads.com/something4");

        GenresOverlapSimilarityCalculator genresCalculatorMock = mock();
        when(genresCalculatorMock.calculateSimilarity(book1, book2))
                .thenReturn(6.0 / 7);
        TFIDFSimilarityCalculator tfidfCalculatorMock = mock();
        when(tfidfCalculatorMock.calculateSimilarity(book1, book2))
                .thenReturn(0.2346);

        weights.put(genresCalculatorMock, 0.55);
        weights.put(tfidfCalculatorMock, 0.45);
    }

    @Test
    public void calculateSimilarityThrowsOnNullBooks() {
        SimilarityCalculator calculator = new CompositeSimilarityCalculator(weights);
        Book reference = new Book("", "", "", "", List.of(), 0, 0, "");

        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateSimilarity(null, reference),
                "calculateSimilarity() should throw on null book");
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateSimilarity(reference, null),
                "calculateSimilarity() should throw on null book");
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateSimilarity(null, null),
                "calculateSimilarity() should throw on null book");
    }

    @Test
    public void calculateSimilarityCalculatesCorrectly() {
        SimilarityCalculator calculator = new CompositeSimilarityCalculator(weights);
        double genresWeighted = (6.0 / 7) * 0.55;
        double tfidfWeighted = 0.2346 * 0.45;

        assertEquals(genresWeighted + tfidfWeighted, calculator.calculateSimilarity(book1, book2));
    }
}
