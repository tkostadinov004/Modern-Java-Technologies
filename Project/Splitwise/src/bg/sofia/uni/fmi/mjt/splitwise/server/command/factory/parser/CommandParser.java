package bg.sofia.uni.fmi.mjt.splitwise.server.command.factory.parser;

import java.util.Arrays;

public class CommandParser {
    private static final String PARSE_REGEX = "[\s+]";

    public ParsedCommand parse(String input) {
        if (input == null) {
            return null;
        }

        String[] splitted = Arrays.stream(input
                .split(PARSE_REGEX))
                .filter(arg -> !arg.isEmpty() && !arg.isBlank())
                .toArray(String[]::new);
        if (splitted.length == 0) {
            return null;
        }

        return new ParsedCommand(splitted[0],
                Arrays.stream(splitted).skip(1).toList());
    }
}
