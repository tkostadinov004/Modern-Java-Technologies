package bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.comparator;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.SimilarityCalculator;

import java.util.Comparator;

public class BookSimilarityComparator implements Comparator<Book> {
    private final Book origin;
    private final SimilarityCalculator calculator;

    public BookSimilarityComparator(Book origin, SimilarityCalculator calculator) {
        this.origin = origin;
        this.calculator = calculator;
    }

    @Override
    public int compare(Book o1, Book o2) {
        return Double.compare(calculator.calculateSimilarity(o1, origin), calculator.calculateSimilarity(o2, origin));
    }
}
