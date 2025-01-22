package bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception;

public class NonExistingGroupException extends RuntimeException {
    public NonExistingGroupException(String message) {
        super(message);
    }

    public NonExistingGroupException(String message, Throwable cause) {
        super(message, cause);
    }
}
