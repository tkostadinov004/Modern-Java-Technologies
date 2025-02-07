package bg.sofia.uni.fmi.mjt.splitwise.server.command.exception;

public class CommandArgumentsCountException extends RuntimeException {
    public CommandArgumentsCountException(String message) {
        super(message);
    }

    public CommandArgumentsCountException(String message, Throwable cause) {
        super(message);
    }
}
