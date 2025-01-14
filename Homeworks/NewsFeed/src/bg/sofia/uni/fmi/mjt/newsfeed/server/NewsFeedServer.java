package bg.sofia.uni.fmi.mjt.newsfeed.server;

import bg.sofia.uni.fmi.mjt.newsfeed.server.request.command.CommandExecutable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;

public class NewsFeedServer {
    private static final int BUFFER_SIZE = 2048;
    private static final String HOST = "localhost";
    private static final String COMMAND_SPLIT_REGEX = "[\s]+";

    private int port;
    private Selector selector;
    private ByteBuffer buffer;
    private boolean isRunning;

    public NewsFeedServer(int port) {
        this.port = port;
        isRunning = false;
    }

    private void initializeServer(ServerSocketChannel serverSocketChannel) throws IOException {
        selector = Selector.open();
        buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        serverSocketChannel.bind(new InetSocketAddress(HOST, port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        isRunning = true;
        System.out.println("Server running at host: " + HOST + ", port: " + port);
    }

    private void writeOutputToClient(SocketChannel client, String message) throws IOException {
        buffer.clear();
        buffer.put(message.getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        client.write(buffer);
    }

    private void acceptConnection(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel client = serverSocketChannel.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);

        writeOutputToClient(client, "Successfully connected to server!");
        System.out.println("Accepted connection by client " + client.socket().getRemoteSocketAddress());
    }

    private String readClientCommand(SocketChannel client) throws IOException {
        buffer.clear();
        client.read(buffer);
        buffer.flip();
        byte[] bufferBytes = new byte[buffer.remaining()];
        buffer.get(bufferBytes);
        return new String(bufferBytes, StandardCharsets.UTF_8);
    }

    private void handleClientCommand(SocketChannel client) throws IOException {
        String[] commandLine = Arrays.stream(readClientCommand(client)
                .split(COMMAND_SPLIT_REGEX))
                .filter(s -> !s.isEmpty() && !s.isBlank())
                .toArray(String[]::new);
        CommandExecutable command = CommandExecutable.of(commandLine, client, buffer);
        command.execute();
    }

    private void handleSelectionKey(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            acceptConnection(key);
        } else if (key.isReadable()) {
            SocketChannel client = (SocketChannel) key.channel();
            handleClientCommand(client);
        }
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            initializeServer(serverSocketChannel);
            while (isRunning) {
                int keysCount = selector.select();
                if (keysCount == 0) {
                    continue;
                }

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    handleSelectionKey(keyIterator.next());
                    keyIterator.remove();
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}

class Program {
    public static void main(String[] args) {
        NewsFeedServer server = new NewsFeedServer(54321);
        server.start();
    }
}