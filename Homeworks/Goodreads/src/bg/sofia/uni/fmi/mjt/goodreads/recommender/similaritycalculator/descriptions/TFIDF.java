package bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.descriptions;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.recommender.similaritycalculator.SimilarityCalculator;
import bg.sofia.uni.fmi.mjt.goodreads.tokenizer.TextTokenizer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TFIDF implements SimilarityCalculator {
    private Set<Book> books;
    private TextTokenizer tokenizer;

    public TFIDF(Set<Book> books, TextTokenizer tokenizer) {
        this.books = books;
        this.tokenizer = tokenizer;
    }

    @Override
    public double calculateSimilarity(Book first, Book second) {
        throw new UnsupportedOperationException();
    }

    public Map<String, Double> computeTFIDF(Book book) {
        Map<String, Double> tf = computeTF(book);
        Map<String, Double> idf = computeIDF(book);

        return tf.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey(),
                        e -> e.getValue() * idf.get(e.getKey())));
    }

    public Map<String, Double> computeTF(Book book) {
        List<String> words = tokenizer.tokenize(book.description());
        return words
                .stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey(),
                        e -> (double)e.getValue() / words.size()));
    }

    public Map<String, Double> computeIDF(Book book) {
        List<String> words = tokenizer.tokenize(book.description());
        return words
                .stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey(),
                        e -> Math.log((double)books.size() / books
                                .stream()
                                .filter(b -> tokenizer.tokenize(b.description()).contains(e.getKey()))
                                .toList()
                                .size())));
    }

}