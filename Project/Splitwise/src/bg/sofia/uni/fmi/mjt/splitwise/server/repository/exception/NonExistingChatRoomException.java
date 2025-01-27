package bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception;

public class NonExistingChatRoomException extends RuntimeException {
    public NonExistingChatRoomException(String message) {
        super(message);
    }

    public NonExistingChatRoomException(String message, Throwable cause) {
        super(message, cause);
    }
}
