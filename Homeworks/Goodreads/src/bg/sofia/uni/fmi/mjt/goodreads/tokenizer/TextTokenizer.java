package bg.sofia.uni.fmi.mjt.goodreads.tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TextTokenizer {
    private final Set<String> stopwords;

    public TextTokenizer(Reader stopwordsReader) {
        try (var br = new BufferedReader(stopwordsReader)) {
            stopwords = br.lines().collect(Collectors.toSet());
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not load dataset", ex);
        }
    }

    public List<String> tokenize(String input) {
        if (input.isEmpty()) {
            return List.of();
        }

        final String punctRegex = "\\p{Punct}";

        return Arrays.stream(input
                        .replaceAll(punctRegex, "")
                        .toLowerCase()
                        .split("[\\s]+"))
                        .filter(word -> !stopwords.contains(word))
                        .toList();
    }

    public Set<String> stopwords() {
        return stopwords;
    }
}