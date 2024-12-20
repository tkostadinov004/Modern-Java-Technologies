package bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.genres;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.SimilarityCalculator;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GenresOverlapSimilarityCalculatorTest {
    @Test
    public void calculateSimilarityThrowsOnNullBooks() {
        SimilarityCalculator calculator = new GenresOverlapSimilarityCalculator();
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
        SimilarityCalculator calculator = new GenresOverlapSimilarityCalculator();
        Book book1 = new Book("101", "The Hobbit", "J.R.R. Tolkien",
                "In a hole in the ground there lived a hobbit. Not a nasty, dirty, wet hole, filled with the ends of worms and an oozy smell, nor yet a dry, bare, sandy hole with nothing in it to sit down on or to eat: it was a hobbit-hole, and that means comfort.Written for J.R.R. TolkienвЂ™s own children, The Hobbit met with instant critical acclaim when it was first published in 1937. Now recognized as a timeless classic, this introduction to the hobbit Bilbo Baggins, the wizard Gandalf, Gollum, and the spectacular world of Middle-earth recounts of the adventures of a reluctant hero, a powerful and dangerous ring, and the cruel dragon Smaug the Magnificent. The text in this 372-page paperback edition is based on that first published in Great Britain by Collins Modern Classics (1998), and includes a note on the text by Douglas A. Anderson (2001).\n",
                List.of("Fantasy", "Classics", "Fiction", "Adventure", "Young Adult", "Science Fiction Fantasy", "High Fantasy"),
                4.33, 7963002, "goodreads.com/something1");
        Book book2 = new Book("104", "The Lord of the Rings", "J.R.R. Tolkien",
                "One Ring to rule them all, One Ring to find them, One Ring to bring them all and in the darkness bind themIn ancient times the Rings of Power were crafted by the Elven-smiths, and Sauron, the Dark Lord, forged the One Ring, filling it with his own power so that he could rule all others. But the One Ring was taken from him, and though he sought it throughout Middle-earth, it remained lost to him. After many ages it fell by chance into the hands of the hobbit Bilbo Baggins.From Sauron's fastness in the Dark Tower of Mordor, his power spread far and wide. Sauron gathered all the Great Rings to him, but always he searched for the One Ring that would complete his dominion.When Bilbo reached his eleventy-first birthday he disappeared, bequeathing to his young cousin Frodo the Ruling Ring and a perilous quest: to journey across Middle-earth, deep into the shadow of the Dark Lord, and destroy the Ring by casting it into the Cracks of Doom.The Lord of the Rings tells of the great quest undertaken by Frodo and the Fellowship of the Ring: Gandalf the Wizard; the hobbits Merry, Pippin, and Sam; Gimli the Dwarf; Legolas the Elf; Boromir of Gondor; and a tall, mysterious stranger called Strider.\n",
                List.of("Fantasy", "Classics", "Fiction", "Adventure", "Science Fiction Fantasy", "High Fantasy", "Epic Fantasy"),
                4.34, 722312,"goodreads.com/something4");

        Set<String> intersection = new HashSet<>(book1.genres());
        intersection.retainAll(book2.genres());

        int lesserSize = Math.min(book1.genres().size(), book2.genres().size());
        double expectedOverlapCoefficient = intersection.size() * 1.0 / lesserSize;

        assertEquals(expectedOverlapCoefficient,
                calculator.calculateSimilarity(book1, book2),
                "Two books should be compared by the similarity in their respective genre sets.");
    }

    @Test
    public void calculateSimilarityShouldReturnZeroIfOneOfTheBooksHasNoGenres() {
        SimilarityCalculator calculator = new GenresOverlapSimilarityCalculator();
        Book book1 = new Book("101", "The Hobbit", "J.R.R. Tolkien",
                "In a hole in the ground there lived a hobbit. Not a nasty, dirty, wet hole, filled with the ends of worms and an oozy smell, nor yet a dry, bare, sandy hole with nothing in it to sit down on or to eat: it was a hobbit-hole, and that means comfort.Written for J.R.R. TolkienвЂ™s own children, The Hobbit met with instant critical acclaim when it was first published in 1937. Now recognized as a timeless classic, this introduction to the hobbit Bilbo Baggins, the wizard Gandalf, Gollum, and the spectacular world of Middle-earth recounts of the adventures of a reluctant hero, a powerful and dangerous ring, and the cruel dragon Smaug the Magnificent. The text in this 372-page paperback edition is based on that first published in Great Britain by Collins Modern Classics (1998), and includes a note on the text by Douglas A. Anderson (2001).\n",
                List.of(),
                4.33, 7963002, "goodreads.com/something1");
        Book book2 = new Book("104", "The Lord of the Rings", "J.R.R. Tolkien",
                "One Ring to rule them all, One Ring to find them, One Ring to bring them all and in the darkness bind themIn ancient times the Rings of Power were crafted by the Elven-smiths, and Sauron, the Dark Lord, forged the One Ring, filling it with his own power so that he could rule all others. But the One Ring was taken from him, and though he sought it throughout Middle-earth, it remained lost to him. After many ages it fell by chance into the hands of the hobbit Bilbo Baggins.From Sauron's fastness in the Dark Tower of Mordor, his power spread far and wide. Sauron gathered all the Great Rings to him, but always he searched for the One Ring that would complete his dominion.When Bilbo reached his eleventy-first birthday he disappeared, bequeathing to his young cousin Frodo the Ruling Ring and a perilous quest: to journey across Middle-earth, deep into the shadow of the Dark Lord, and destroy the Ring by casting it into the Cracks of Doom.The Lord of the Rings tells of the great quest undertaken by Frodo and the Fellowship of the Ring: Gandalf the Wizard; the hobbits Merry, Pippin, and Sam; Gimli the Dwarf; Legolas the Elf; Boromir of Gondor; and a tall, mysterious stranger called Strider.\n",
                List.of("Fantasy", "Classics", "Fiction", "Adventure", "Science Fiction Fantasy", "High Fantasy", "Epic Fantasy"),
                4.34, 722312,"goodreads.com/something4");

        assertEquals(0,
                calculator.calculateSimilarity(book1, book2),
                "Two books cannot be similar if one of the books doesn't have any genres");
        assertEquals(0,
                calculator.calculateSimilarity(book2, book1),
                "Two books cannot be similar if one of the books doesn't have any genres");
    }

    @Test
    public void calculateSimilarityShouldReturnOneIfBothTheBooksHaveNoGenres() {
        SimilarityCalculator calculator = new GenresOverlapSimilarityCalculator();
        Book book1 = new Book("101", "The Hobbit", "J.R.R. Tolkien",
                "In a hole in the ground there lived a hobbit. Not a nasty, dirty, wet hole, filled with the ends of worms and an oozy smell, nor yet a dry, bare, sandy hole with nothing in it to sit down on or to eat: it was a hobbit-hole, and that means comfort.Written for J.R.R. TolkienвЂ™s own children, The Hobbit met with instant critical acclaim when it was first published in 1937. Now recognized as a timeless classic, this introduction to the hobbit Bilbo Baggins, the wizard Gandalf, Gollum, and the spectacular world of Middle-earth recounts of the adventures of a reluctant hero, a powerful and dangerous ring, and the cruel dragon Smaug the Magnificent. The text in this 372-page paperback edition is based on that first published in Great Britain by Collins Modern Classics (1998), and includes a note on the text by Douglas A. Anderson (2001).\n",
                List.of(),
                4.33, 7963002, "goodreads.com/something1");
        Book book2 = new Book("104", "The Lord of the Rings", "J.R.R. Tolkien",
                "One Ring to rule them all, One Ring to find them, One Ring to bring them all and in the darkness bind themIn ancient times the Rings of Power were crafted by the Elven-smiths, and Sauron, the Dark Lord, forged the One Ring, filling it with his own power so that he could rule all others. But the One Ring was taken from him, and though he sought it throughout Middle-earth, it remained lost to him. After many ages it fell by chance into the hands of the hobbit Bilbo Baggins.From Sauron's fastness in the Dark Tower of Mordor, his power spread far and wide. Sauron gathered all the Great Rings to him, but always he searched for the One Ring that would complete his dominion.When Bilbo reached his eleventy-first birthday he disappeared, bequeathing to his young cousin Frodo the Ruling Ring and a perilous quest: to journey across Middle-earth, deep into the shadow of the Dark Lord, and destroy the Ring by casting it into the Cracks of Doom.The Lord of the Rings tells of the great quest undertaken by Frodo and the Fellowship of the Ring: Gandalf the Wizard; the hobbits Merry, Pippin, and Sam; Gimli the Dwarf; Legolas the Elf; Boromir of Gondor; and a tall, mysterious stranger called Strider.\n",
                List.of(),
                4.34, 722312,"goodreads.com/something4");

        assertEquals(1,
                calculator.calculateSimilarity(book1, book2),
                "Two books should be completely similar if both of them don't have any genres");
    }
}
