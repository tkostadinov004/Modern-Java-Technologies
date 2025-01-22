package bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception;

public class FriendGroupException extends RuntimeException {
    public FriendGroupException(String message) {
        super(message);
    }

    public FriendGroupException(String message, Throwable cause) {
        super(message, cause);
    }
}
