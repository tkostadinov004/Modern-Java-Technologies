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
    private static final String PUNCT_REGEX = "\\p{Punct}"; //"\\p{Punct}&&[^\']"
    private static final String WHITESPACE_REGEX = "[\\s]+";

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

        return Arrays.stream(input
                        .replaceAll(PUNCT_REGEX, "")
                        .toLowerCase()
                        .split(WHITESPACE_REGEX))
                        .filter(word -> !stopwords.contains(word))
                        .toList();
    }

    public Set<String> stopwords() {
        return stopwords;
    }
}