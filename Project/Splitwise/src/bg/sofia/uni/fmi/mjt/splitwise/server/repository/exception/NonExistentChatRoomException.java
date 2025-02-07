package bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception;

import bg.sofia.uni.fmi.mjt.splitwise.server.chat.exception.ChatException;

public class NonExistentChatRoomException extends ChatException {
    public NonExistentChatRoomException(String message) {
        super(message);
    }

    public NonExistentChatRoomException(String message, Throwable cause) {
        super(message, cause);
    }
}
