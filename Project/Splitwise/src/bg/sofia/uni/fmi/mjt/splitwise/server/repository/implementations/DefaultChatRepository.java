package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.chat.ChatCodeGenerator;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.ChatServer;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ChatMessagesRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ChatRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingChatRoomException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingUserException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultChatRepository implements ChatRepository {
    private final InetSocketAddress mainServerAddress;
    private final UserRepository userRepository;
    private final Map<String, ChatServer> chatServers;
    private final Map<Socket, Socket> socketUsers;
    private final Map<Socket, Socket> userSockets;

    public DefaultChatRepository(InetSocketAddress mainServerAddress, UserRepository userRepository) {
        this.mainServerAddress = mainServerAddress;
        this.userRepository = userRepository;
        this.chatServers = new HashMap<>();
        this.socketUsers = new HashMap<>();
        this.userSockets = new HashMap<>();
    }

    @Override
    public Socket connectUser(String username, String roomCode) throws ChatException {
        if (!userRepository.containsUser(username)) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(username));
        }
        if (!containsRoom(roomCode)) {
            throw new NonExistingChatRoomException("Chat room with code %s does not exist".formatted(roomCode));
        }

        ChatServer server = chatServers.get(roomCode);

        Socket socket = new Socket();
        try {
            socket.connect(server.address());
        } catch (IOException e) {
            throw new ChatException(e.getMessage(), e);
        }
        socketUsers.put(socket, userRepository.getSocketByUsername(username).get());
        userSockets.put(userRepository.getSocketByUsername(username).get(), socket);
        return socket;
    }

    @Override
    public void sendMessage(String senderUsername, String roomCode, String message) {
        Optional<Socket> senderSocket = userRepository.getSocketByUsername(senderUsername);
        if (senderSocket.isEmpty()) {
            throw new NonExistingUserException("User is not currently logged in!");
        }
        if (!containsRoom(roomCode)) {
            throw new NonExistingChatRoomException("Chat room with code %s does not exist".formatted(roomCode));
        }

        ChatServer chatServer = chatServers.get(roomCode);
        chatServer.sendMessage(message, userRepository.getUserByUsername(senderUsername).get(), userSockets.get(senderSocket.get()), socketUsers);
    }

    @Override
    public boolean containsRoom(String roomCode) {
        if (roomCode == null || roomCode.isEmpty() || roomCode.isBlank()) {
            throw new IllegalArgumentException("Room code cannot be null, blank or empty!");
        }

        return chatServers.containsKey(roomCode);
    }

    @Override
    public Optional<ChatServer> getByCode(String roomCode) {
        if (!containsRoom(roomCode)) {
            return Optional.empty();
        }

        return Optional.of(chatServers.get(roomCode));
    }

    @Override
    public void shutdownRoom(String roomCode) {
        if (!containsRoom(roomCode)) {
            throw new NonExistingChatRoomException("Chat room with code %s does not exist".formatted(roomCode));
        }

        ChatServer server = chatServers.get(roomCode);
        server.shutdown();
        chatServers.remove(roomCode);
    }

    @Override
    public String createRoom(ChatMessagesRepository chatMessagesRepository) throws ChatException {
        ChatCodeGenerator generator = new ChatCodeGenerator();
        String code;
        while (chatServers.containsKey(code = generator.generateRandom())) {

        }
        ChatServer server = new ChatServer(mainServerAddress, code, chatMessagesRepository);
        try {
            server.start();
        } catch (IOException e) {
            throw new ChatException(e.getMessage(), e);
        }

        chatServers.put(code, server);
        return code;
    }

    @Override
    public void disconnectUser(String username, String roomCode) throws ChatException {
        Optional<ChatServer> server = getByCode(roomCode);
        if (server.isPresent()) {
            try {
                server.get().disconnectUser(userRepository.getSocketByUsername(username).get());
            } catch (IOException e) {
                throw new ChatException(e.getMessage());
            }
        }
    }
}
