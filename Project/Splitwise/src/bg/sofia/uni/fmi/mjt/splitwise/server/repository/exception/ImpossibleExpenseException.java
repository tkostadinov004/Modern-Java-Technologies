package bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception;

public class ImpossibleExpenseException extends RuntimeException {
    public ImpossibleExpenseException(String message) {
        super(message);
    }

    public ImpossibleExpenseException(String message, Throwable cause) {
        super(message, cause);
    }
}
