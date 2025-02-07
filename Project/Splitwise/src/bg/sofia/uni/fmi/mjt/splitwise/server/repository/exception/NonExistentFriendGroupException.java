package bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception;

public class NonExistentFriendGroupException extends RuntimeException {
    public NonExistentFriendGroupException(String message) {
        super(message);
    }

    public NonExistentFriendGroupException(String message, Throwable cause) {
        super(message, cause);
    }
}
