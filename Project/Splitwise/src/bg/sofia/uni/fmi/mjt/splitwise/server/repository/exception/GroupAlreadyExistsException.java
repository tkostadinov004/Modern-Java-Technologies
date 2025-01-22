package bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception;

public class GroupAlreadyExistsException extends RuntimeException {
    public GroupAlreadyExistsException(String message) {
        super(message);
    }

    public GroupAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
