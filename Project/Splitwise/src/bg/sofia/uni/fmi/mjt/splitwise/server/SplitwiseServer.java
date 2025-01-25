package bg.sofia.uni.fmi.mjt.splitwise.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

public class SplitwiseServer {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    private void configureServer(ServerSocketChannel serverSocketChannel) throws IOException {
        serverSocketChannel.bind(new InetSocketAddress(HOST, PORT));

    }
    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Program {
    public static void main(String[] args) {

    }
}