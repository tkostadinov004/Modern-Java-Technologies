package bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.genres;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.SimilarityCalculator;

import java.util.ArrayList;
import java.util.List;

public class GenresOverlapSimilarityCalculator implements SimilarityCalculator {
    @Override
    public double calculateSimilarity(Book first, Book second) {
        if (first == null) {
            throw new IllegalArgumentException("The first book cannot be null!");
        }
        if (second == null) {
            throw new IllegalArgumentException("The second book cannot be null!");
        }

        if (first.genres().isEmpty() || second.genres().isEmpty()) {
            return 0;
        }

        List<String> genreIntersection = new ArrayList<>(first.genres());
        genreIntersection.retainAll(second.genres());

        return genreIntersection.size() / Math.min(first.genres().size(), second.genres().size());
    }
}