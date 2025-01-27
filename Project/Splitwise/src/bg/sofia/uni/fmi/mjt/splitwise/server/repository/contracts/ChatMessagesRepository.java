package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.chat.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.ChatServer;

import java.util.List;

public interface ChatMessagesRepository {
    void writeMessage(ChatServer server, String message) throws ChatException;

    List<String> getMessages(ChatServer server) throws ChatException;
}
