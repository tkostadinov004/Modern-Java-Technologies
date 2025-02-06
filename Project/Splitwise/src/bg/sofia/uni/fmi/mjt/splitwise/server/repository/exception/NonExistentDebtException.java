package bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception;

public class NonExistentDebtException extends RuntimeException {
    public NonExistentDebtException(String message) {
        super(message);
    }

    public NonExistentDebtException(String message, Throwable cause) {
        super(message, cause);
    }
}
