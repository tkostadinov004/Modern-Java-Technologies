package bg.sofia.uni.fmi.mjt.splitwise.server.chat;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.NotAuthenticatedException;

public interface ChatToken {
    boolean isInChat();

    ChatServer getServer() throws ChatException;

    void joinChat(String chatCode) throws ChatException, NotAuthenticatedException;

    void leaveChat() throws ChatException;
}
