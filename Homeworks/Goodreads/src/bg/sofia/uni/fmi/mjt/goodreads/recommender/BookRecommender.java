package bg.sofia.uni.fmi.mjt.goodreads.recommender;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.SimilarityCalculator;
import bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.comparator.BookSimilarityComparator;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BookRecommender implements BookRecommenderAPI {
    private Set<Book> initialBooks;
    private SimilarityCalculator calculator;

    public BookRecommender(Set<Book> initialBooks, SimilarityCalculator calculator) {
        this.initialBooks = initialBooks;
        this.calculator = calculator;
    }

    @Override
    public SortedMap<Book, Double> recommendBooks(Book origin, int maxN) {
        if (origin == null) {
            throw new IllegalArgumentException("Origin book cannot be null!");
        }
        if (maxN <= 0) {
            throw new IllegalArgumentException("The amount of books returned shouldn't be less than or equal to zero!");
        }

        Supplier<TreeMap<Book, Double>> treeMapSupplier = () ->
                new TreeMap<>(new BookSimilarityComparator(origin, calculator).reversed());

        return initialBooks
                .stream()
                .collect(Collectors.toMap((Book book) -> book,
                        book -> calculator.calculateSimilarity(book, origin),
                        (e1, e2) -> e1,
                        treeMapSupplier));
    }
}