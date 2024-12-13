package bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.composite;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.SimilarityCalculator;

import java.util.Map;
import java.util.stream.Collectors;

public class CompositeSimilarityCalculator implements SimilarityCalculator {
    private Map<SimilarityCalculator, Double> similarityCalculatorMap;

    public CompositeSimilarityCalculator(Map<SimilarityCalculator, Double> similarityCalculatorMap) {
        this.similarityCalculatorMap = similarityCalculatorMap;
    }

    @Override
    public double calculateSimilarity(Book first, Book second) {
        if (first == null) {
            throw new IllegalArgumentException("The first book cannot be null!");
        }
        if (second == null) {
            throw new IllegalArgumentException("The second book cannot be null!");
        }

        return similarityCalculatorMap
                .entrySet()
                .stream()
                .map(entry -> entry.getKey().calculateSimilarity(first, second) * entry.getValue())
                .collect(Collectors.summingDouble(Double::doubleValue));
    }
}