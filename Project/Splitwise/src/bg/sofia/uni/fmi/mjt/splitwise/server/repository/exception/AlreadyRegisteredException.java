package bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception;

public class AlreadyRegisteredException extends RuntimeException {
    public AlreadyRegisteredException(String message) {
        super(message);
    }

    public AlreadyRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }
}
