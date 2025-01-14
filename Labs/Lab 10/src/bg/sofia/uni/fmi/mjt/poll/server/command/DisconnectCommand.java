package bg.sofia.uni.fmi.mjt.poll.server.command;

import bg.sofia.uni.fmi.mjt.poll.server.response.Response;
import bg.sofia.uni.fmi.mjt.poll.server.response.StatusCode;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class DisconnectCommand implements Command {
    private final SocketChannel client;

    public DisconnectCommand(SocketChannel client) {
        this.client = client;
    }

    @Override
    public Response execute() {
        try {
            client.close();
        } catch (IOException e) {
            return new Response(StatusCode.ERROR,
                    "message",
                    "Unexpected connection error");
        }

        return new Response(StatusCode.OK,
                "message",
                "\"Successfully disconnected from server\"");
    }
}
