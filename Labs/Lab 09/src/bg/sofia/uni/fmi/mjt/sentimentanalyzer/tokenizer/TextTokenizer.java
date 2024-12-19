package bg.sofia.uni.fmi.mjt.sentimentanalyzer.tokenizer;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class TextTokenizer {
    private final Set<String> stopwords;
    private static final String PUNCT_REGEX = "\\p{Punct}";
    private static final String WHITESPACE_REGEX = "[\\s]+";

    public TextTokenizer(Set<String> stopwords) {
        this.stopwords = stopwords;
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
}
