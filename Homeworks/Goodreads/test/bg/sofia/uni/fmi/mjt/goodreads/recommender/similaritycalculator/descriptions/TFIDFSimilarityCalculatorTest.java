package bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.descriptions;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.SimilarityCalculator;
import bg.sofia.uni.fmi.mjt.goodreads.tokenizer.TextTokenizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TFIDFSimilarityCalculatorTest {
    private static Set<Book> books;
    private static TextTokenizer tokenizer;

    @BeforeAll
    public static void setUp() {
        Book[] referenceBooks = new Book[7];
        referenceBooks[0] = new Book("100", "The Alchemist", "Paulo Coelho",
                "Combining magic, mysticism, wisdom, and wonder into an inspiring tale of self-discovery, The Alchemist has become a modern classic, selling millions of copies around the world and transforming the lives of countless readers across generations.Paulo Coelho's masterpiece tells the mystical story of Santiago, an Andalusian shepherd boy who yearns to travel in search of a worldly treasure. His quest will lead him to riches far differentвЂ”and far more satisfyingвЂ”than he ever imagined. Santiago's journey teaches us about the essential wisdom of listening to our hearts, recognizing opportunity and learning to read the omens strewn along life's path, and, most importantly, following our dreams.\n",
                List.of("Fiction", "Classics", "Fantasy", "Philosophy", "Novels", "Spirituality", "Self-Help"),
                3.9, 2792673, "goodreads.com/something");
        referenceBooks[1] = new Book("101", "The Hobbit", "J.R.R. Tolkien",
                "In a hole in the ground there lived a hobbit. Not a nasty, dirty, wet hole, filled with the ends of worms and an oozy smell, nor yet a dry, bare, sandy hole with nothing in it to sit down on or to eat: it was a hobbit-hole, and that means comfort.Written for J.R.R. TolkienвЂ™s own children, The Hobbit met with instant critical acclaim when it was first published in 1937. Now recognized as a timeless classic, this introduction to the hobbit Bilbo Baggins, the wizard Gandalf, Gollum, and the spectacular world of Middle-earth recounts of the adventures of a reluctant hero, a powerful and dangerous ring, and the cruel dragon Smaug the Magnificent. The text in this 372-page paperback edition is based on that first published in Great Britain by Collins Modern Classics (1998), and includes a note on the text by Douglas A. Anderson (2001).\n",
                List.of("Fantasy", "Classics", "Fiction", "Adventure", "Young Adult", "Science Fiction Fantasy", "High Fantasy"),
                4.33, 7963002, "goodreads.com/something1");
        referenceBooks[2] = new Book("102", "Hamlet", "William Shakespeare",
                "Among Shakespeare's plays, \"Hamlet\" is considered by many his masterpiece. Among actors, the role of Hamlet, Prince of Denmark, is considered the jewel in the crown of a triumphant theatrical career. Now Kenneth Branagh plays the leading role and co-directs a brillant ensemble performance. Three generations of legendary leading actors, many of whom first assembled for the Oscar-winning film \"Henry V\", gather here to perform the rarely heard complete version of the play. This clear, subtly nuanced, stunning dramatization, presented by The Renaissance Theatre Company in association with \"Bbc\" Broadcasting, features such luminaries as Sir John Gielgud, Derek Jacobi, Emma Thompson and Christopher Ravenscroft. It combines a full cast with stirring music and sound effects to bring this magnificent Shakespearen classic vividly to life. Revealing new riches with each listening, this production of \"Hamlet\" is an invaluable aid for students, teachers and all true lovers of Shakespeare - a recording to be treasured for decades to come.\n",
                List.of("Classics", "Fiction", "Plays", "Drama", "School", "Literature", "Theatre"),
                4.02, 890747,"goodreads.com/something2");
        referenceBooks[3] = new Book("103", "The Stand", "Stephen King",
                "First came the days of the plague. Then came the dreams. Dark dreams that warned of the coming of the dark man. The apostate of death, his worn-down boot heels tramping the night roads. The warlord of the charnel house and Prince of Evil. His time is at hand. His empire grows in the west and the Apocalypse looms.For hundreds of thousands of fans who read The Stand in its original version and wanted more, this new edition is Stephen King's gift. And those who are listening to The Stand for the first time will discover a triumphant and eerily plausible work of the imagination that takes on the issues that will determine our survival.\n",
                List.of("Horror", "Fiction", "Fantasy", "Science Fiction", "Post Apocalyptic", "Thriller", "Dystopia"),
                4.54, 312353,"goodreads.com/something3");
        referenceBooks[4] = new Book("104", "The Lord of the Rings", "J.R.R. Tolkien",
                "One Ring to rule them all, One Ring to find them, One Ring to bring them all and in the darkness bind themIn ancient times the Rings of Power were crafted by the Elven-smiths, and Sauron, the Dark Lord, forged the One Ring, filling it with his own power so that he could rule all others. But the One Ring was taken from him, and though he sought it throughout Middle-earth, it remained lost to him. After many ages it fell by chance into the hands of the hobbit Bilbo Baggins.From Sauron's fastness in the Dark Tower of Mordor, his power spread far and wide. Sauron gathered all the Great Rings to him, but always he searched for the One Ring that would complete his dominion.When Bilbo reached his eleventy-first birthday he disappeared, bequeathing to his young cousin Frodo the Ruling Ring and a perilous quest: to journey across Middle-earth, deep into the shadow of the Dark Lord, and destroy the Ring by casting it into the Cracks of Doom.The Lord of the Rings tells of the great quest undertaken by Frodo and the Fellowship of the Ring: Gandalf the Wizard; the hobbits Merry, Pippin, and Sam; Gimli the Dwarf; Legolas the Elf; Boromir of Gondor; and a tall, mysterious stranger called Strider.\n",
                List.of("Fantasy", "Classics", "Fiction", "Adventure", "Science Fiction Fantasy", "High Fantasy", "Epic Fantasy"),
                4.34, 722312,"goodreads.com/something4");
        referenceBooks[5] = new Book("105", "Perfect (Second Opportunities, #2)", "Judith McNaught",
                "A rootless foster child, Julie Mathison had blossomed under the love showered upon her by her adoptive family. Now a lovely and vivacious young woman, she was a respected teacher in her small Texas town, and she passionately lived her ideals. Julie was determined to give back all the kindness she'd received; nothing and no one would ever shatter the perfect life she had fashioned.  Zachary Benedict was an actor/director whose Academy Award-winning career had been shattered when he was wrongly convicted of murdering his wife. After the tall, ruggedly handsome Zack escaped from a Texas prison, he abducted Julie and forced her to drive him to his Colorado mountain hideout. She was outraged, cautious, and unable to ignore the instincts that whispered of his innocence. He was cynical, wary, and increasingly attracted to her. Passion was about to capture them both in its fierce embrace...but the journey to trust, true commitment, and proving Zack's innocence was just beginning....\n",
                List.of("Romance", "Contemporary Romance", "Contemporary", "Fiction", "Historical Romance", "Chick Lit", "Romantic Suspense"),
                4.3, 23822, "goodreads.com/something5");
        referenceBooks[6] = new Book("", "", "",
                "The story of the love between a brave hobbit, a ring, and thousands of hobbit generations", List.of(), 0, 0, "");

        String stopwords = "a\nabout\nabove\nafter\nagain\nagainst\nall\nam\nan\nand\nany\nare\naren't\nas\nat\nbe\nbecause\nbeen\nbefore\nbeing\nbelow\nbetween\nboth\nbut\nby\ncan't\ncannot\ncould\ncouldn't\ndid\ndidn't\ndo\ndoes\ndoesn't\ndoing\ndon't\ndown\nduring\neach\nfew\nfor\nfrom\nfurther\nhad\nhadn't\nhas\nhasn't\nhave\nhaven't\nhaving\nhe\nhe'd\nhe'll\nhe's\nher\nhere\nhere's\nhers\nherself\nhim\nhimself\nhis\nhow\nhow's\ni\ni'd\ni'll\ni'm\ni've\nif\nin\ninto\nis\nisn't\nit\nit's\nits\nitself\nlet's\nme\nmore\nmost\nmustn't\nmy\nmyself\nno\nnor\nnot\nof\noff\non\nonce\nonly\nor\nother\nought\nour\nours\nourselves\nout\nover\nown\nsame\nshan't\nshe\nshe'd\nshe'll\nshe's\nshould\nshouldn't\nso\nsome\nsuch\nthan\nthat\nthat's\nthe\ntheir\ntheirs\nthem\nthemselves\nthen\nthere\nthere's\nthese\nthey\nthey'd\nthey'll\nthey're\nthey've\nthis\nthose\nthrough\nto\ntoo\nunder\nuntil\nup\nvery\nwas\nwasn't\nwe\nwe'd\nwe'll\nwe're\nwe've\nwere\nweren't\nwhat\nwhat's\nwhen\nwhen's\nwhere\nwhere's\nwhich\nwhile\nwho\nwho's\nwhom\nwhy\nwhy's\nwith\nwon't\nwould\nwouldn't\nyou\nyou'd\nyou'll\nyou're\nyou've\nyour\nyours\nyourself\nyourselves\n";

        books = Set.of(referenceBooks);
        tokenizer = new TextTokenizer(new StringReader(stopwords));

    }

    @Test
    public void calculateSimilarityThrowsOnNullBooks() {
        SimilarityCalculator  calculator = new TFIDFSimilarityCalculator(books, tokenizer);
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

    private double cosineSimilarity(Map<String, Double> first, Map<String, Double> second) {
        double magnitudeFirst = magnitude(first.values());
        double magnitudeSecond = magnitude(second.values());

        return dotProduct(first, second) / (magnitudeFirst * magnitudeSecond);
    }

    private double dotProduct(Map<String, Double> first, Map<String, Double> second) {
        Set<String> commonKeys = new HashSet<>(first.keySet());
        commonKeys.retainAll(second.keySet());

        return commonKeys.stream()
                .mapToDouble(word -> first.get(word) * second.get(word))
                .sum();
    }

    private double magnitude(Collection<Double> input) {
        double squaredMagnitude = input.stream()
                .map(v -> v * v)
                .reduce(0.0, Double::sum);

        return Math.sqrt(squaredMagnitude);
    }

    @Test
    public void calculateSimilarityReturnsCorrectly() {
        TFIDFSimilarityCalculator  calculator = new TFIDFSimilarityCalculator(books, tokenizer);
        Book book1 = new Book("", "", "",
                "The ground love hobbit love ring hobbit generations hobbit", List.of(), 0, 0, "");
        Book book2 = new Book("", "", "",
                "The story of the love between a brave hobbit, a ring, and thousands of hobbit generations", List.of(), 0, 0, "");

        final double booksCount = books.size();
        final double filteredDescriptionWordsCountFirst = 8;

        Map<String, Double> expectedTFFirst = Map.of("ground", 1 / filteredDescriptionWordsCountFirst, "love", 2 / filteredDescriptionWordsCountFirst, "ring", 1 / filteredDescriptionWordsCountFirst, "hobbit", 3 / filteredDescriptionWordsCountFirst, "generations", 1 / filteredDescriptionWordsCountFirst);
        Map<String, Double> expectedIDFFirst = Map.of("ground", Math.log(booksCount / 1), "love", Math.log(booksCount / 2), "ring", Math.log(booksCount / 3), "hobbit", Math.log(booksCount / 3), "generations", Math.log(booksCount / 2));

        Map<String, Double> expectedTFIDFFirst = Map.of("ground", expectedTFFirst.get("ground") * expectedIDFFirst.get("ground"),
                "love", expectedTFFirst.get("love") * expectedIDFFirst.get("love"),
                "hobbit", expectedTFFirst.get("hobbit") * expectedIDFFirst.get("hobbit"),
                "ring", expectedTFFirst.get("ring") * expectedIDFFirst.get("ring"),
                "generations", expectedTFFirst.get("generations") * expectedIDFFirst.get("generations"));

        final double filteredDescriptionWordsCountSecond = 8;

        Map<String, Double> expectedTFSecond = Map.of("story", 1 / filteredDescriptionWordsCountSecond, "love", 1 / filteredDescriptionWordsCountSecond, "brave", 1 / filteredDescriptionWordsCountSecond, "hobbit", 2 / filteredDescriptionWordsCountSecond, "ring", 1 / filteredDescriptionWordsCountSecond, "thousands", 1 / filteredDescriptionWordsCountSecond, "generations", 1 / filteredDescriptionWordsCountSecond);
        Map<String, Double> expectedIDFSecond = Map.of("story", Math.log(booksCount / 2), "love", Math.log(booksCount / 2), "brave", Math.log(booksCount / 1), "hobbit", Math.log(booksCount / 3), "ring", Math.log(booksCount / 3), "thousands", Math.log(booksCount / 2), "generations", Math.log(booksCount / 2));
        Map<String, Double> expectedTFIDFSecond = Map.of("story", expectedTFSecond.get("story") * expectedIDFSecond.get("story"),
                "love", expectedTFSecond.get("love") * expectedIDFSecond.get("love"),
                "brave", expectedTFSecond.get("brave") * expectedIDFSecond.get("brave"),
                "hobbit", expectedTFSecond.get("hobbit") * expectedIDFSecond.get("hobbit"),
                "ring", expectedTFSecond.get("ring") * expectedIDFSecond.get("ring"),
                "thousands", expectedTFSecond.get("thousands") * expectedIDFSecond.get("thousands"),
                "generations", expectedTFSecond.get("generations") * expectedIDFSecond.get("generations"));

        assertEquals(cosineSimilarity(expectedTFIDFFirst, expectedTFIDFSecond), calculator.calculateSimilarity(book1, book2));
    }

    @Test
    public void computeTFReturnsEmptyMapOnEmptyDescription() {
        TFIDFSimilarityCalculator calculator = new TFIDFSimilarityCalculator(books, tokenizer);
        Book reference = new Book("", "", "", "", List.of(), 0, 0, "");

        assertTrue(calculator.computeTF(reference).isEmpty(),
                "The result of TF computation of a book with no description should be an empty map");
    }

    @Test
    public void computeTFReturnsCorrectly() {
        TFIDFSimilarityCalculator calculator = new TFIDFSimilarityCalculator(books, tokenizer);
        Book reference = new Book("", "", "",
                "The book that shows us the book that exists, regardless of their genre or genre preference of the reader", List.of(), 0, 0, "");
        final double filteredCount = 10;
        Map<String, Double> expected = Map.of("book", 2 / filteredCount, "shows", 1 / filteredCount, "exists", 1 / filteredCount, "regardless", 1 / filteredCount, "genre", 2 / filteredCount, "preference", 1 / filteredCount, "reader", 1/ filteredCount, "us", 1/ filteredCount);
        assertEquals(expected, calculator.computeTF(reference));
    }

    @Test
    public void computeIDFReturnsEmptyMapOnEmptyDescription() {
        TFIDFSimilarityCalculator calculator = new TFIDFSimilarityCalculator(books, tokenizer);
        Book reference = new Book("", "", "", "", List.of(), 0, 0, "");

        assertTrue(calculator.computeIDF(reference).isEmpty(),
                "The result of IDF computation of a book with no description should be an empty map");
    }

    @Test
    public void computeIDFReturnsCorrectly() {
        TFIDFSimilarityCalculator calculator = new TFIDFSimilarityCalculator(books, tokenizer);
        Book reference = new Book("", "", "",
                "The ground love ring hobbit generations", List.of(), 0, 0, "");
        final double booksCount = books.size();
        Map<String, Double> expected = Map.of("ground", Math.log(booksCount / 1), "love", Math.log(booksCount / 2), "ring", Math.log(booksCount / 3), "hobbit", Math.log(booksCount / 3), "generations", Math.log(booksCount / 2));
        assertEquals(expected, calculator.computeIDF(reference));
    }

    @Test
    public void computeTFIDFReturnsEmptyMapOnEmptyDescription() {
        TFIDFSimilarityCalculator calculator = new TFIDFSimilarityCalculator(books, tokenizer);
        Book reference = new Book("", "", "", "", List.of(), 0, 0, "");

        assertTrue(calculator.computeTFIDF(reference).isEmpty(),
                "The result of TFIDF computation of a book with no description should be an empty map");
    }

    @Test
    public void computeTFIDFReturnsCorrectly() {
        TFIDFSimilarityCalculator calculator = new TFIDFSimilarityCalculator(books, tokenizer);
        Book reference = new Book("", "", "",
                "The ground love hobbit love ring hobbit generations hobbit", List.of(), 0, 0, "");
        final double booksCount = books.size();
        final double filteredDescriptionWordsCount = 8;
        Map<String, Double> expectedTF = Map.of("ground", 1 / filteredDescriptionWordsCount, "love", 2 / filteredDescriptionWordsCount, "ring", 1 / filteredDescriptionWordsCount, "hobbit", 3 / filteredDescriptionWordsCount, "generations", 1 / filteredDescriptionWordsCount);
        Map<String, Double> expectedIDF = Map.of("ground", Math.log(booksCount / 1), "love", Math.log(booksCount / 2), "ring", Math.log(booksCount / 3), "hobbit", Math.log(booksCount / 3), "generations", Math.log(booksCount / 2));

        Map<String, Double> expectedTFIDF = Map.of("ground", expectedTF.get("ground") * expectedIDF.get("ground"),
                "love", expectedTF.get("love") * expectedIDF.get("love"),
                "hobbit", expectedTF.get("hobbit") * expectedIDF.get("hobbit"),
                "ring", expectedTF.get("ring") * expectedIDF.get("ring"),
                "generations", expectedTF.get("generations") * expectedIDF.get("generations"));
        assertEquals(expectedTFIDF, calculator.computeTFIDF(reference));
    }
}
