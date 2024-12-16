package bg.sofia.uni.fmi.mjt.goodreads.finder;

import bg.sofia.uni.fmi.mjt.goodreads.book.Book;
import bg.sofia.uni.fmi.mjt.goodreads.tokenizer.TextTokenizer;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BookFinder implements BookFinderAPI {
    private Set<Book> books;
    private TextTokenizer tokenizer;

    public BookFinder(Set<Book> books, TextTokenizer tokenizer) {
        this.books = books;
        this.tokenizer = tokenizer;
    }

    public Set<Book> allBooks() {
        return books;
    }

    @Override
    public List<Book> searchByAuthor(String authorName) {
        if (authorName == null || authorName.isEmpty()) {
            throw new IllegalArgumentException("Author name cannot be null or empty!");
        }

        return books
                .stream()
                .filter(book -> book.author().equals(authorName))
                .toList();
    }

    @Override
    public Set<String> allGenres() {
        return books
                .stream()
                .map(book -> book.genres())
                .flatMap(genres -> genres.stream())
                .collect(Collectors.toSet());
    }

    private Predicate<Book> getGenreCriteria(Set<String> genres, MatchOption option) {
        return switch (option) {
            case MATCH_ALL ->
                    (Book book) -> genres
                            .stream()
                            .allMatch(genre -> book.genres().contains(genre));
            case MATCH_ANY ->
                    (Book book) -> genres
                            .stream()
                            .anyMatch(genre -> book.genres().contains(genre));
        };
    }

    @Override
    public List<Book> searchByGenres(Set<String> genres, MatchOption option) {
        if (genres == null) {
            throw new IllegalArgumentException("Genres cannot be null!");
        }
        Predicate<Book> genreMatchCriteria = getGenreCriteria(genres, option);

        return books
                .stream()
                .filter(genreMatchCriteria)
                .toList();
    }

    private Predicate<Book> getKeywordCriteria(Set<String> keywords, MatchOption option) {
        return switch (option) {
            case MATCH_ALL ->
                    (Book book) -> keywords
                            .stream()
                            .allMatch(keyword ->
                                    tokenizer.tokenize(book.description()).contains(keyword) ||
                                    tokenizer.tokenize(book.title()).contains(keyword));
            case MATCH_ANY ->
                    (Book book) -> keywords
                            .stream()
                            .anyMatch(keyword ->
                                    tokenizer.tokenize(book.description()).contains(keyword) ||
                                            tokenizer.tokenize(book.title()).contains(keyword));
        };
    }

    @Override
    public List<Book> searchByKeywords(Set<String> keywords, MatchOption option) {
        Predicate<Book> keywordMatchCriteria = getKeywordCriteria(keywords, option);

        return books
                .stream()
                .filter(keywordMatchCriteria)
                .toList();
    }

}