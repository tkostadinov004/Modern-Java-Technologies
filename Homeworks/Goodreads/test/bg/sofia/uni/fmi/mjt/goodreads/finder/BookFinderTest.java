package bg.sofia.uni.fmi.mjt.goodreads.finder;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.tokenizer.TextTokenizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookFinderTest {
    private static BookFinderAPI bookFinder;
    private static Book[] referenceBooks;

    @BeforeAll
    public static void setUp() {
        referenceBooks = new Book[6];
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

        final String stopwords = "a\nabout\nabove\nafter\nagain\nagainst\nall\nam\nan\nand\nany\nare\narent\nas\nat\nbe\nbecause\nbeen\nbefore\nbeing\nbelow\nbetween\nboth\nbut\nby\ncant\ncannot\ncould\ncouldnt\ndid\ndidnt\ndo\ndoes\ndoesnt\ndoing\ndont\ndown\nduring\neach\nfew\nfor\nfrom\nfurther\nhad\nhadnt\nhas\nhasnt\nhave\nhavent\nhaving\nhe\nhed\nhell\nhes\nher\nhere\nheres\nhers\nherself\nhim\nhimself\nhis\nhow\nhows\ni\nid\nill\nim\nive\nif\nin\ninto\nis\nisnt\nit\nits\nits\nitself\nlets\nme\nmore\nmost\nmustnt\nmy\nmyself\nno\nnor\nnot\nof\noff\non\nonce\nonly\nor\nother\nought\nour\nours\nourselves\nout\nover\nown\nsame\nshant\nshe\nshed\nshell\nshes\nshould\nshouldnt\nso\nsome\nsuch\nthan\nthat\nthats\nthe\ntheir\ntheirs\nthem\nthemselves\nthen\nthere\ntheres\nthese\nthey\ntheyd\ntheyll\ntheyre\ntheyve\nthis\nthose\nthrough\nto\ntoo\nunder\nuntil\nup\nvery\nwas\nwasnt\nwe\nwed\nwell\nwere\nweve\nwere\nwerent\nwhat\nwhats\nwhen\nwhens\nwhere\nwheres\nwhich\nwhile\nwho\nwhos\nwhom\nwhy\nwhys\nwith\nwont\nwould\nwouldnt\nyou\nyoud\nyoull\nyoure\nyouve\nyour\nyours\nyourself\nyourselves\n";
        
        Set<Book> books = Set.of(referenceBooks);
        bookFinder = new BookFinder(books, new TextTokenizer(new StringReader(stopwords)));
    }

    @Test
    public void allGenresReturnsAllGenresSuccessfully() {
        Set<String> expected = Set.of("Fiction", "Classics", "Fantasy", "Philosophy", "Novels", "Spirituality", "Self-Help", "Adventure", "Young Adult", "Science Fiction Fantasy", "High Fantasy", "Plays", "Drama", "School", "Literature", "Theatre", "Horror", "Science Fiction", "Post Apocalyptic", "Thriller", "Dystopia", "Epic Fantasy", "Romance", "Contemporary Romance", "Contemporary", "Historical Romance", "Chick Lit", "Romantic Suspense");
        assertEquals(expected, bookFinder.allGenres(),
                "allGenres() should return the distinct genres of each book");
    }

    @Test
    public void searchByAuthorThrowsOnNullOrEmptyName() {
        assertThrows(IllegalArgumentException.class,
                () -> bookFinder.searchByAuthor(null),
                "searchByAuthor(name) should throw on null name");
        assertThrows(IllegalArgumentException.class,
                () -> bookFinder.searchByAuthor(""),
                "searchByAuthor(name) should throw on empty name");
    }

    @Test
    public void searchByAuthorReturnsEmptyCollectionWithNonexistentAuthor() {
        List<Book> result = bookFinder.searchByAuthor("abcd");
        assertTrue(result.isEmpty(),
                "The collection of books with an author not present in the database should be empty");
    }

    @Test
    public void searchByAuthorReturnsCorrectly() {
        List<Book> result = bookFinder.searchByAuthor("J.R.R. Tolkien");
        List<Book> expected = List.of(referenceBooks[1], referenceBooks[4]);
        assertEquals(expected.size(), result.size(),
                "The size of the list of books with a given author should be 2");
        assertTrue(result.containsAll(expected),
                "The list of books with a given author should contain all books, written from this author in the database");
    }

    @Test
    public void searchByGenresThrowsOnNullGenres() {
        assertThrows(IllegalArgumentException.class,
                () -> bookFinder.searchByGenres(null, MatchOption.MATCH_ALL),
                "searchByGenres(genres, option) should throw on null genres set");
    }

    @Test
    public void searchByGenresReturnsCorrectlyWithMatchAny() {
        List<Book> result = bookFinder.searchByGenres(Set.of("Fantasy", "Classics"),
                MatchOption.MATCH_ANY);
        List<Book> expected = List.of(referenceBooks[0], referenceBooks[1], referenceBooks[2], referenceBooks[3], referenceBooks[4]);
        assertEquals(expected.size(), result.size(),
                "The list should contain all books with at least one of the provided genres");
        assertTrue(result.containsAll(expected),
                "The list should contain all books with at least one of the provided genres");
    }

    @Test
    public void searchByGenresReturnsCorrectlyWithMatchAll() {
        List<Book> result = bookFinder.searchByGenres(Set.of("Fantasy", "Classics"),
                MatchOption.MATCH_ALL);
        List<Book> expected = List.of(referenceBooks[0], referenceBooks[1], referenceBooks[4]);
        assertEquals(expected.size(), result.size(),
                "The list should contain all books with all of the provided genres");
        assertTrue(result.containsAll(expected),
                "The list should contain all books with all of the provided genres");
    }

    @Test
    public void searchByKeywordsReturnsCorrectlyWithMatchAny() {
        List<Book> result = bookFinder.searchByKeywords(Set.of("middle", "ring", "hamlet"),
                MatchOption.MATCH_ANY);
        List<Book> expected = List.of(referenceBooks[1], referenceBooks[2],  referenceBooks[4]);
        assertEquals(expected.size(), result.size(),
                "The list should contain all books containing at least one of the provided keywords in their title or description");
        assertTrue(result.containsAll(expected),
                "The list should contain all books containing at least one of the provided keywords in their title or description");
    }

    @Test
    public void searchByKeywordsReturnsCorrectlyWithMatchAll() {
        List<Book> result = bookFinder.searchByKeywords(Set.of("middleearth", "ring", "sauron"),
                MatchOption.MATCH_ALL);
        List<Book> expected = List.of(referenceBooks[4]);
        assertEquals(expected.size(), result.size(),
                "The list should contain all books containing all of the provided keywords in their title or description");
        assertTrue(result.containsAll(expected),
                "The list should contain all books containing all of the provided keywords in their title or description");
    }
}
