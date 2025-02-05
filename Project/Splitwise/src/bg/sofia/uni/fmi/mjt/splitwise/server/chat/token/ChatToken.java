package bg.sofia.uni.fmi.mjt.splitwise.server.chat.token;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.NotAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.exception.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.DefaultChatServer;

public interface ChatToken {
    boolean isInChat();

    DefaultChatServer getServer() throws ChatException;

    void joinChat(String chatCode) throws ChatException, NotAuthenticatedException;

    void leaveChat() throws ChatException;
}
