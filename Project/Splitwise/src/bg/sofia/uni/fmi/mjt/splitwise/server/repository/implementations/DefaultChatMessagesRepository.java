package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.chat.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.ChatServer;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ChatMessagesRepository;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DefaultChatMessagesRepository implements ChatMessagesRepository {
    private final Map<ChatServer, List<String>> messages;
    private static final int MESSAGES_MAX_SIZE = 20;

    public DefaultChatMessagesRepository() {
        this.messages = new HashMap<>();
    }

    @Override
    public void writeMessage(ChatServer server, String message) throws ChatException {
        messages.putIfAbsent(server, new LinkedList<>());
        if (messages.get(server).size() == MESSAGES_MAX_SIZE) {
            messages.get(server).remove(0);
        }
        messages.get(server).add(message);
    }

    @Override
    public List<String> getMessages(ChatServer server) throws ChatException {
        return messages.get(server);
    }
}
