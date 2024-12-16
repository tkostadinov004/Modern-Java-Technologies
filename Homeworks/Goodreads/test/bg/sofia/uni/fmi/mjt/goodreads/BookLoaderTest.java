package bg.sofia.uni.fmi.mjt.goodreads;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Set;

public class BookLoaderTest {
    @Test
    public void loadsSuccessfully() {
        String csv = "N,Book,Author,Description,Genres,Avg_Rating,Num_Ratings,URL\n0,To Kill a Mockingbird,Harper Lee,\"The unforgettable novel of a childhood in a sleepy Southern town and the crisis of conscience that rocked it. \"To Kill A Mockingbird\" became both an instant bestseller and a critical success when it was first published in 1960. It went on to win the Pulitzer Prize in 1961 and was later made into an Academy Award-winning film, also a classic.Compassionate, dramatic, and deeply moving, \"To Kill A Mockingbird\" takes readers to the roots of human behavior - to innocence and experience, kindness and cruelty, love and hatred, humor and pathos. Now with over 18 million copies in print and translated into forty languages, this regional story by a young Alabama woman claims universal appeal. Harper Lee always considered her book to be a simple love story. Today it is regarded as a masterpiece of American literature.\",\"['Classics', 'Fiction', 'Historical Fiction', 'School', 'Literature', 'Young Adult', 'Historical']\",4.27,5691311,https://www.goodreads.com/book/show/2657.To_Kill_a_Mockingbird\n"
                + "1,\"Harry Potter and the Philosopher's Stone (Harry Potter, #1)\",J.K. Rowling,\"Harry Potter thinks he is an ordinary boy - until he is rescued by an owl, taken to Hogwarts School of Witchcraft and Wizardry, learns to play Quidditch and does battle in a deadly duel. The Reason ... HARRY POTTER IS A WIZARD!\",\"['Fantasy', 'Fiction', 'Young Adult', 'Magic', 'Childrens', 'Middle Grade', 'Classics']\",4.47,9278135,https://www.goodreads.com/book/show/72193.Harry_Potter_and_the_Philosopher_s_Stone";

        Set<Book> books = BookLoader.load(new StringReader(csv));
        Book book1 = new Book("0", "To Kill a Mockingbird", "Harper Lee",
                "The unforgettable novel of a childhood in a sleepy Southern town and the crisis of conscience that rocked it. \"To Kill A Mockingbird\" became both an instant bestseller and a critical success when it was first published in 1960. It went on to win the Pulitzer Prize in 1961 and was later made into an Academy Award-winning film, also a classic.Compassionate, dramatic, and deeply moving, \"To Kill A Mockingbird\" takes readers to the roots of human behavior - to innocence and experience, kindness and cruelty, love and hatred, humor and pathos. Now with over 18 million copies in print and translated into forty languages, this regional story by a young Alabama woman claims universal appeal. Harper Lee always considered her book to be a simple love story. Today it is regarded as a masterpiece of American literature.",
                List.of("Classics", "Fiction", "Historical Fiction", "School", "Literature", "Young Adult", "Historical"),
                4.27, 5691311, "https://www.goodreads.com/book/show/2657.To_Kill_a_Mockingbird");
        Book book2 = new Book("1", "Harry Potter and the Philosopher's Stone (Harry Potter, #1)", "J.K. Rowling",
                "Harry Potter thinks he is an ordinary boy - until he is rescued by an owl, taken to Hogwarts School of Witchcraft and Wizardry, learns to play Quidditch and does battle in a deadly duel. The Reason ... HARRY POTTER IS A WIZARD!",
                List.of("Fantasy", "Fiction", "Young Adult", "Magic", "Childrens", "Middle Grade", "Classics"),
                4.47, 9278135, "https://www.goodreads.com/book/show/72193.Harry_Potter_and_the_Philosopher_s_Stone");
        Assertions.assertEquals(Set.of(book1, book2), books);
    }
}
