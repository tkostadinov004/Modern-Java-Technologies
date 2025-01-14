package bg.sofia.uni.fmi.mjt.newsfeed.server.request.command;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class HelpCommand implements CommandExecutable {
    private final SocketChannel client;
    private final ByteBuffer buffer;

    public HelpCommand(SocketChannel client, ByteBuffer buffer) {
        this.client = client;
        this.buffer = buffer;
    }

    @Override
    public void execute() throws IOException {
        buffer.clear();
        buffer.put("testHelp".getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        client.write(buffer);
    }
}
