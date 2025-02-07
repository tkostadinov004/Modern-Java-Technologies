package bg.sofia.uni.fmi.mjt.splitwise.server.command.factory.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandParser {
    private String[] split(String input) {
        List<String> result = new ArrayList<>();
        StringBuilder curr = new StringBuilder();
        boolean isInQuotes = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\"') {
                isInQuotes = !isInQuotes;
            } else if (isInQuotes || !Character.isWhitespace(c)) {
                curr.append(c);
            } else if (Character.isWhitespace(c) && !curr.isEmpty()) {
                result.add(curr.toString());
                curr.setLength(0);
            }
        }

        if (!curr.isEmpty()) {
            result.add(curr.toString());
        }

        return result.toArray(String[]::new);
    }

    public ParsedCommand parse(String input) {
        if (input == null) {
            return null;
        }

        String[] splitted = split(input);
        if (splitted.length == 0) {
            return null;
        }

        return new ParsedCommand(splitted[0],
                Arrays.stream(splitted).skip(1).toArray(String[]::new));
    }
}
