package bg.sofia.uni.fmi.mjt.splitwise.server.command.factory;

import bg.sofia.uni.fmi.mjt.splitwise.server.command.factory.parser.CommandParser;

public interface Factory<T> {
    T build(String input, CommandParser parser);
}
