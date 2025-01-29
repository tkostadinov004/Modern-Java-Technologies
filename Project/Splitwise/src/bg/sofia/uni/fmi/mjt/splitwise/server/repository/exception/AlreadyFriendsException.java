package bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception;

public class AlreadyFriendsException extends RuntimeException {
    public AlreadyFriendsException(String message) {
        super(message);
    }

    public AlreadyFriendsException(String message, Throwable cause) {
        super(message, cause);
    }
}
