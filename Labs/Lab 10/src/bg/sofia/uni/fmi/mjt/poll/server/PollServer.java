package bg.sofia.uni.fmi.mjt.poll.server;

import bg.sofia.uni.fmi.mjt.poll.server.command.Command;
import bg.sofia.uni.fmi.mjt.poll.server.repository.PollRepository;
import bg.sofia.uni.fmi.mjt.poll.server.response.Response;
import bg.sofia.uni.fmi.mjt.poll.server.response.StatusCode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class PollServer {
    private static final int BUFFER_SIZE = 2048;
    private static final String HOST = "localhost";

    private final int port;
    private PollRepository pollRepository;
    private Selector selector;
    private ByteBuffer buffer;
    private boolean isRunning;

    public PollServer(int port, PollRepository pollRepository) {
        this.port = port;
        this.pollRepository = pollRepository;
        this.isRunning = false;
    }

    private void acceptConnection(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void writeResponseToClient(Response response, SocketChannel client) throws IOException {
        buffer.clear();
        buffer.put(response.toString().getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        client.write(buffer);
    }

    private void handleClientInput(SelectionKey selectionKey) throws IOException {
        SocketChannel client = (SocketChannel) selectionKey.channel();
        buffer.clear();
        if (client.read(buffer) < 0) {
            client.close();
            return;
        }

        buffer.flip();
        byte[] inputBytes = new byte[buffer.remaining()];
        buffer.get(inputBytes);

        String input = new String(inputBytes, StandardCharsets.UTF_8);
        Command command = Command.of(input, pollRepository, client);

        Response response;
        if (command == null) {
            response = new Response(StatusCode.ERROR, "message", "Invalid command");
        } else {
            response = command.execute();
        }
        writeResponseToClient(response, client);
    }

    private void handleSelectionKey(SelectionKey selectionKey) throws IOException {
        if (selectionKey.isAcceptable()) {
            acceptConnection(selectionKey);
        } else if (selectionKey.isReadable()) {
            handleClientInput(selectionKey);
        }
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
            serverSocketChannel.bind(new InetSocketAddress(HOST, port));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            isRunning = true;

            while (isRunning) {
                int selectedKeys = selector.select();
                if (selectedKeys == 0) {
                    continue;
                }

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    handleSelectionKey(iterator.next());
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        isRunning = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }
}

/*
class Program {
    public static void main(String[] args) {
        PollServer server = new PollServer(7777, new InMemoryPollRepository());
        server.start();
    }
}
*/