package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.chat.exception.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.ChatServer;

import java.net.Socket;
import java.util.Optional;

public interface ChatRepository {
    Socket connectUser(String username, String roomCode) throws ChatException;

    void sendMessage(String senderUsername, String roomCode, String message);

    boolean containsRoom(String roomCode);

    Optional<ChatServer> getByCode(String roomCode);

    void shutdownRoom(String roomCode);

    String createRoom() throws ChatException;

    void disconnectUser(String username, String roomCode) throws ChatException;
}
