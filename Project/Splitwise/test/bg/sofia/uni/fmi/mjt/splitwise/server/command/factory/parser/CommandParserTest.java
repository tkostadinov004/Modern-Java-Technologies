package bg.sofia.uni.fmi.mjt.splitwise.server.command.factory.parser;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class CommandParserTest {
    @Test
    public void testParseParsesCorrectlyWithNoArguments() {
        String input = "   example   ";
        ParsedCommand expected = new ParsedCommand("example", new String[]{});
        ParsedCommand actual = new CommandParser().parse(input);

        assertEquals(expected.name(), actual.name(),
                "When the input has only 1 element, there should be no arguments");
        assertIterableEquals(Arrays.stream(expected.args()).toList(), Arrays.stream(actual.args()).toList(),
                "When the input has only 1 element, there should be no arguments");
    }

    @Test
    public void testParseParsesCorrectlyWithArguments() {
        String input = "   example \"test test\"  str";
        ParsedCommand expected = new ParsedCommand("example", new String[]{"test test", "str"});
        ParsedCommand actual = new CommandParser().parse(input);

        assertEquals(expected.name(), actual.name(),
                "When the input has arguments they should be separated and the quote signs removed");
        assertIterableEquals(Arrays.stream(expected.args()).toList(), Arrays.stream(actual.args()).toList(),
                "When the input has arguments they should be separated and the quote signs removed");
    }

    @Test
    public void testParseReturnsNullWhenThereIsNothingToSplit() {
        String input = "    ";
        ParsedCommand expected = null;
        ParsedCommand actual = new CommandParser().parse(input);

        assertEquals(expected, actual,
                "When the input is blank or empty, null should be returned");
    }

    @Test
    public void testParseReturnsNullWhenStringIsNull() {
        String input = null;
        ParsedCommand expected = null;
        ParsedCommand actual = new CommandParser().parse(input);

        assertEquals(expected, actual,
                "When the input is null, null should be returned");
    }
}
