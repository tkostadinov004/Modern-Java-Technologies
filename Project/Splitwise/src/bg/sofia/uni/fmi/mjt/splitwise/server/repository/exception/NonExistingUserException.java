package bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception;

public class NonExistingUserException extends RuntimeException {
    public NonExistingUserException(String message) {
        super(message);
    }

    public NonExistingUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
