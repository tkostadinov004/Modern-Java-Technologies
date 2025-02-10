package bg.sofia.uni.fmi.mjt.goodreads.book;

import java.util.Arrays;
import java.util.List;

public record Book(
        String ID,
        String title,
        String author,
        String description,
        List<String> genres,
        double rating,
        int ratingCount,
        String URL
) {
    private static List<String> parseGenres(String genres) {
        return genres.contains("'") ? Arrays.stream(genres
                        .substring(1, genres.length() - 1)
                        .split(", "))
                .map(genre -> genre.substring(1, genre.length() - 1))
                .toList() : List.of();
    }

    public static Book of(String[] tokens) {
        if (tokens == null) {
            throw new IllegalArgumentException("Tokens array cannot be null!");
        }

        final int lineLength = 8;
        if (tokens.length != lineLength) {
            throw new IllegalArgumentException("Invalid line length!");
        }
        final int idIndex = 0;
        final int titleIndex = 1;
        final int authorIndex = 2;
        final int descriptionIndex = 3;
        final int genresIndex = 4;
        final int ratingIndex = 5;
        final int ratingCountIndex = 6;
        final int urlIndex = 7;
        List<String> genres = parseGenres(tokens[genresIndex]);
        int ratingCount = Integer.parseInt(tokens[ratingCountIndex]
                .replaceAll(",", ""));
        return new Book(tokens[idIndex],
                tokens[titleIndex],
                tokens[authorIndex],
                tokens[descriptionIndex],
                genres,
                Double.parseDouble(tokens[ratingIndex]),
                ratingCount,
                tokens[urlIndex]);
    }
}