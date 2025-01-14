package bg.sofia.uni.fmi.mjt.newsfeed.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

public class NewsFeedClient {
    private static final String END_COMMAND = "end";
    private static final String COMMAND_LINE_PARSE_REGEX = "[\s]+";
    private static final int BUFFER_SIZE = 4096;
    private static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private static SocketChannel connect(String[] args) throws IOException {
        if (args.length == 1) {
            throw new IllegalArgumentException("IP and port missing");
        } else if (args.length == 2) {
            throw new IllegalArgumentException("IP or port missing");
        }

        String ip = args[1];
        int port = -1;
        try {
            port = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Port should be an integer between 0 and 65535!", e);
        }
        SocketChannel result = SocketChannel.open(new InetSocketAddress(ip, port));
        result.configureBlocking(false);
        return result;
    }

    private static void sendRequestToServer(SocketChannel clientSocketChannel, String input) throws IOException {
        buffer.clear();
        buffer.put(input.getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        clientSocketChannel.write(buffer);
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        NewsFeedClientGUI gui = new NewsFeedClientGUI(System.out);
        gui.printIntroduction();
        SocketChannel clientSocketChannel = null;

        String input;
        while ((input = sc.nextLine()) != END_COMMAND) {
            String[] commandLineArgs = Arrays.stream(input
                    .split(COMMAND_LINE_PARSE_REGEX))
                    .filter(arg -> !arg.isBlank() && !arg.isEmpty()).toArray(String[]::new);
            if (commandLineArgs.length == 0) {
                continue;
            }

            if (commandLineArgs[0].equals("connect")) {
                clientSocketChannel = connect(commandLineArgs);
                buffer.clear();
                while (buffer.hasRemaining()) {
                    int read = clientSocketChannel.read(buffer);
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
            } else if (clientSocketChannel != null) {
                if (commandLineArgs[0].equals("disconnect")) {
                    clientSocketChannel.close();
                    clientSocketChannel = null;
                } else {
                    sendRequestToServer(clientSocketChannel, input);
                    buffer.clear();
                    while (buffer.hasRemaining()) {
                        int read = clientSocketChannel.read(buffer);
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
            } else {
                System.out.println("You have to be connected first!");
            }
        }
        gui.shutdown();
    }
}
