package bg.sofia.uni.fmi.mjt.splitwise.server.chat;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatServer {
    private final InetSocketAddress mainServerAddress;
    private final String code;
    private AtomicBoolean isActive;
    private Set<Socket> clients;
    private InetSocketAddress address;

    public ChatServer(InetSocketAddress mainServerAddress, String code) {
        this.mainServerAddress = mainServerAddress;
        this.code = code;
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
            Optional<Socket> receiverSocketUser = socketUsers.keySet().stream().filter(v -> v.getLocalPort() == receiver.getPort()).findFirst();
            if (receiverSocketUser.isEmpty()) {
                continue;
            }
            try {
                PrintWriter writer = new PrintWriter(socketUsers.get(receiverSocketUser.get()).getOutputStream(), true);
                writer.println("Chat - <%s>: %s".formatted(author, message));
                writer.println("$end$");
            } catch (IOException e) {
                System.out.println("err");
            }
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
        Optional<Socket> socketToRemove = clients.stream().filter(c -> c.getLocalPort() == socket.getPort() && c.getPort() == socket.getLocalPort()).findFirst();
        if (socketToRemove.isEmpty()) {
            System.out.println("ddz");
        }
        socketToRemove.get().close();
        clients.remove(socketToRemove.get());
        if (clients.isEmpty()) {
            shutdown();
        }
    }
}
