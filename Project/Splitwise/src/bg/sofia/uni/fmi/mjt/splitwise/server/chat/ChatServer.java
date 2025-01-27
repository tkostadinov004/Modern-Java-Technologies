package bg.sofia.uni.fmi.mjt.splitwise.server.chat;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ChatMessagesRepository;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ChatServer {
    private final InetSocketAddress mainServerAddress;
    private final String code;
    private final ChatMessagesRepository chatMessagesRepository;
    private AtomicBoolean isActive;
    private Set<Socket> clients;
    private InetSocketAddress address;

    public ChatServer(InetSocketAddress mainServerAddress, String code, ChatMessagesRepository chatMessagesRepository) {
        this.mainServerAddress = mainServerAddress;
        this.code = code;
        this.chatMessagesRepository = chatMessagesRepository;
        this.isActive = new AtomicBoolean(false);
        this.clients = new HashSet<>();
    }

    public String code() {
        return code;
    }

    private void accept(ServerSocket serverSocket) throws IOException {
        while (isActive.get()) {
            Socket client = serverSocket.accept();
            synchronized (clients) {
                clients.add(client);
            }
        }
    }

    public void sendMessage(String message, User author, Socket authorSocket, Map<Socket, Socket> socketUsers) {
        for (Socket receiver : clients) {
/*
            var r = receiver.getRemoteSocketAddress();
            var a = authorSocket.getRemoteSocketAddress();

            var r1 = receiver.getInetAddress();
            var a2 = authorSocket.getInetAddress();

            if (receiver.getInetAddress().equals(authorSocket.getInetAddress())) {
                if (receiver.getLocalPort() == authorSocket.getPort()) {
                    if (receiver.getPort() == authorSocket.getLocalPort()) {
                        continue;
                    }
                }
            }
 */

            var curr = socketUsers.keySet().stream().filter(v -> v.getLocalPort() == receiver.getPort()).collect(Collectors.toSet());
            System.out.println(curr.size());

            if (curr.isEmpty()) {
                continue;
            }

            curr.forEach(socket -> {
                try {
                    PrintWriter writer = new PrintWriter(socketUsers.get(socket).getOutputStream(), true);
                    writer.println("Chat - <%s>: %s".formatted(author, message));
                    writer.println("$end$");
                } catch (IOException e) {
                    System.out.println("opa");
                }
            });
        }
    }

    public void start() throws IOException {
        InetSocketAddress serverAddress = new InetSocketAddress(mainServerAddress.getAddress(), 0);

        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(serverAddress);

        isActive.set(true);
        this.address = new InetSocketAddress(serverSocket.getInetAddress(), serverSocket.getLocalPort());

        Thread serverThread = new Thread(() -> {
            while (isActive.get()) {
                try {
                    accept(serverSocket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        serverThread.start();
    }

    public void shutdown() {
        isActive.set(false);
    }

    public InetSocketAddress address() {
        return address;
    }

    public synchronized void disconnectUser(Socket socket) throws IOException {
        socket.close();
        clients.remove(socket);
        if (clients.isEmpty()) {
            shutdown();
        }
    }
}
