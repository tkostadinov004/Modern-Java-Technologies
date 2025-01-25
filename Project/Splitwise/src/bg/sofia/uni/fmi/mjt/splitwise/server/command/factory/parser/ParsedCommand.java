package bg.sofia.uni.fmi.mjt.splitwise.server.command.factory.parser;

import java.util.List;

public record ParsedCommand(String name, List<String> args) {
}
