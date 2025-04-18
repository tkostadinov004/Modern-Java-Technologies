package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.chat.ChatCodeGenerator;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.ChatServer;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.exception.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.DefaultChatServer;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ChatRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentChatRoomException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class DefaultChatRepository implements ChatRepository {
    private final Logger logger;
    private final InetSocketAddress mainServerAddress;
    private final UserRepository userRepository;
    private final Map<String, DefaultChatServer> chatServers;
    private final Map<Socket, Socket> socketUsers;
    private final Map<Socket, Socket> userSockets;

    public DefaultChatRepository(DependencyContainer dependencyContainer, InetSocketAddress mainServerAddress) {
        this.logger = dependencyContainer.get(Logger.class);
        this.mainServerAddress = mainServerAddress;
        this.userRepository = dependencyContainer.get(UserRepository.class);
        this.chatServers = new ConcurrentHashMap<>();
        this.socketUsers = new ConcurrentHashMap<>();
        this.userSockets = new ConcurrentHashMap<>();
    }

    private Socket connectUser(String username, String roomCode, ChatServer server) throws ChatException {
        Socket socket = new Socket();
        try {
            socket.connect(server.address());
        } catch (IOException e) {
            logger.severe(e.getMessage());
            throw new ChatException("Unexpected server error!");
        }
        Optional<Socket> userSocket = userRepository.getSocketByUsername(username);
        if (userSocket.isEmpty()) {
            throw new ChatException("User not found!");
        }
        socketUsers.put(socket, userSocket.get());
        userSockets.put(userSocket.get(), socket);
        logger.info("User %s (%s) connected to room with code %s."
                .formatted(username, userSocket.get().getInetAddress(), roomCode));
        return socket;
    }

    @Override
    public Socket connectUser(String username, String roomCode) throws ChatException {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }
        if (roomCode == null || roomCode.isEmpty() || roomCode.isBlank()) {
            throw new IllegalArgumentException("Room code cannot be null, blank or empty!");
        }
        if (!userRepository.containsUser(username)) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(username));
        }
        if (!containsRoom(roomCode)) {
            throw new NonExistentChatRoomException("Chat room with code %s does not exist".formatted(roomCode));
        }
        ChatServer server = chatServers.get(roomCode);

        return connectUser(username, roomCode, server);
    }

    @Override
    public void sendMessage(String senderUsername, String roomCode, String message)
            throws NonExistentChatRoomException {
        Optional<Socket> senderSocket = userRepository.getSocketByUsername(senderUsername);
        if (senderSocket.isEmpty()) {
            throw new NonExistentUserException("User is not currently logged in!");
        }
        if (!containsRoom(roomCode)) {
            throw new NonExistentChatRoomException("Chat room with code %s does not exist".formatted(roomCode));
        }

        DefaultChatServer chatServer = chatServers.get(roomCode);
        Optional<User> sender = userRepository.getUserByUsername(senderUsername);
        if (sender.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!"
                    .formatted(senderUsername));
        }
        chatServer.sendMessage(message, sender.get(), socketUsers);
        logger.info("User %s (%s) send a message in room with code %s."
                .formatted(senderUsername, senderSocket.get().getInetAddress(), roomCode));
    }

    @Override
    public boolean containsRoom(String roomCode) {
        if (roomCode == null || roomCode.isEmpty() || roomCode.isBlank()) {
            throw new IllegalArgumentException("Room code cannot be null, blank or empty!");
        }

        return chatServers.containsKey(roomCode);
    }

    @Override
    public Optional<DefaultChatServer> getByCode(String roomCode) {
        if (!containsRoom(roomCode)) {
            return Optional.empty();
        }

        return Optional.of(chatServers.get(roomCode));
    }

    @Override
    public void shutdownRoom(String roomCode) throws NonExistentChatRoomException {
        if (!containsRoom(roomCode)) {
            throw new NonExistentChatRoomException("Chat room with code %s does not exist".formatted(roomCode));
        }

        DefaultChatServer server = chatServers.get(roomCode);
        server.shutdown();
        chatServers.remove(roomCode);
        logger.info("Room with code %s was stopped."
                .formatted(roomCode));
    }

    @Override
    public String createRoom() throws ChatException {
        ChatCodeGenerator generator = new ChatCodeGenerator();
        String code;
        while (chatServers.containsKey(code = generator.generateRandom())) {

        }
        DefaultChatServer server = new DefaultChatServer(mainServerAddress, code);
        try {
            server.start();
        } catch (IOException e) {
            throw new ChatException(e.getMessage(), e);
        }

        chatServers.put(code, server);
        logger.info("Room with code %s was created."
                .formatted(code));
        return code;
    }

    @Override
    public void disconnectUser(String username, String roomCode) throws ChatException {
        Optional<DefaultChatServer> server = getByCode(roomCode);
        if (server.isPresent()) {
            try {
                Optional<Socket> socket = userRepository.getSocketByUsername(username);
                if (socket.isEmpty()) {
                    throw new ChatException("Socket not found!");
                }
                server.get().disconnectUser(userSockets.get(socket.get()));
                if (server.get().participantsCount() == 0) {
                    shutdownRoom(roomCode);
                }
                logger.info("User %s disconnected from group with code %s."
                        .formatted(username, roomCode));
            } catch (IOException e) {
                logger.severe(e.getMessage());
                throw new ChatException("Unexpected server error!");
            }
        }
    }
}
