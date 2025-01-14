package bg.sofia.uni.fmi.mjt.newsfeed.server.request.command;

import bg.sofia.uni.fmi.mjt.newsfeed.server.request.command.exception.InvalidCommandException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface CommandExecutable {
    void execute() throws IOException;

    static CommandExecutable of(String[] args, SocketChannel socketChannel, ByteBuffer buffer) {
        String commandName = args[0];
        return switch (commandName) {
            case "help" -> new HelpCommand(socketChannel, buffer);
            case "fetch" -> new FetchCommand(args, socketChannel, buffer);
            default -> throw new InvalidCommandException("Invalid command name!");
        };
    }
}
