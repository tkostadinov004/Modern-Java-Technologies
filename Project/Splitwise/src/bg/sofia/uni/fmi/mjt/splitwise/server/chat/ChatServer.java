package bg.sofia.uni.fmi.mjt.splitwise.server.chat;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

public interface ChatServer {
    String code();

    InetSocketAddress address();

    void sendMessage(String message, User author, Map<Socket, Socket> socketUsers);

    void start() throws IOException;

    void shutdown();

    void disconnectUser(Socket socket) throws IOException;
}
