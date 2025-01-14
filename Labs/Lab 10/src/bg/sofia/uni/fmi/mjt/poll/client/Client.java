/*
package bg.sofia.uni.fmi.mjt.poll.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 2048;

    private static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private static void connect(SocketChannel channel) throws IOException {
        channel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
        channel.configureBlocking(false);
    }
    public static void main(String[] args) {
        try (SocketChannel channel = SocketChannel.open();
                Scanner scanner = new Scanner(System.in)) {
            connect(channel);
            while (true) {
                System.out.print("Enter command: ");
                String command = scanner.nextLine();
                if (command.equals("end")) {
                    break;
                }

                buffer.clear();
                buffer.put(command.getBytes(StandardCharsets.UTF_8));
                buffer.flip();
                channel.write(buffer);

                buffer.clear();

                while (buffer.hasRemaining()) {
                    int read = channel.read(buffer);
                    if (read == -1) {
                        break;
                    } else if (read > 0) {
                        buffer.flip();
                        byte[] response = new byte[buffer.remaining()];
                        buffer.get(response);
                        String responseString = new String(response, StandardCharsets.UTF_8);
                        System.out.println(responseString);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
 */