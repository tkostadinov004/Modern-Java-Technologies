package bg.sofia.uni.fmi.mjt.splitwise.server.dependency.exception;

public class DependencyNotFoundException extends RuntimeException {
    public DependencyNotFoundException(String message) {
        super(message);
    }

    public DependencyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
