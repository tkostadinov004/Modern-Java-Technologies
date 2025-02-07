package bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts;

import bg.sofia.uni.fmi.mjt.splitwise.server.chat.exception.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.DefaultChatServer;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentChatRoomException;

import java.net.Socket;
import java.util.Optional;

public interface ChatRepository {
    Socket connectUser(String username, String roomCode) throws ChatException;

    void sendMessage(String senderUsername, String roomCode, String message) throws NonExistentChatRoomException;

    boolean containsRoom(String roomCode);

    Optional<DefaultChatServer> getByCode(String roomCode) throws NonExistentChatRoomException;

    void shutdownRoom(String roomCode) throws NonExistentChatRoomException;

    String createRoom() throws ChatException;

    void disconnectUser(String username, String roomCode) throws ChatException;
}
