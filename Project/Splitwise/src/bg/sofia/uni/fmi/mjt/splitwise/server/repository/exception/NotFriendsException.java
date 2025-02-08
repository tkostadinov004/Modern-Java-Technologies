package bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception;

public class NotFriendsException extends RuntimeException {
    public NotFriendsException(String message) {
        super(message);
    }

    public NotFriendsException(String message, Throwable cause) {
        super(message, cause);
    }
}
