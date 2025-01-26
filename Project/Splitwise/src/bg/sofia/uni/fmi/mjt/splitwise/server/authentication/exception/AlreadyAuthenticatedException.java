package bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception;

public class AlreadyAuthenticatedException extends Exception {
    public AlreadyAuthenticatedException(String message) {
        super(message);
    }

    public AlreadyAuthenticatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
