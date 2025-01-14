package bg.sofia.uni.fmi.mjt.newsfeed.server.request.command;

import bg.sofia.uni.fmi.mjt.newsfeed.server.request.command.builder.FetchCommandBuilder;
import bg.sofia.uni.fmi.mjt.newsfeed.server.request.command.exception.InvalidCommandException;
import bg.sofia.uni.fmi.mjt.newsfeed.server.request.secure.ApiKeyLoader;
import bg.sofia.uni.fmi.mjt.newsfeed.server.request.secure.ApiSecurityException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class FetchCommand implements CommandExecutable {
    private final String[] args;
    private final SocketChannel client;
    private final ByteBuffer buffer;

    public FetchCommand(String[] args, SocketChannel client, ByteBuffer buffer) {
        this.args = args;
        this.client = client;
        this.buffer = buffer;
    }

    private FetchCommandBuilder getCommandBuilder() {
        FetchCommandBuilder builder = new FetchCommandBuilder();
        for (String arg : Arrays.stream(args).skip(1).toList()) {
            String[] keyValue = arg.split("=");
            String key = keyValue[0];
            String value = keyValue[1];

            switch (key) {
                case "sources" -> builder.filterSources(value);
                case "country" -> builder.filterCountry(value);
                case "category" -> builder.filterCategory(value);
                case "keyword" -> builder.filterByKeyword(value);
                case "pageSize" -> {
                    int pageValue = 0;
                    try {
                        pageValue = Integer.parseInt(value);
                        builder.paginate(pageValue);
                    } catch (NumberFormatException e) {
                        throw new InvalidCommandException("Invalid page size!");
                    }
                }
                default -> throw new InvalidCommandException("Invalid fetch key!");
            }
        }
        return builder;
    }

    private HttpRequest createRequest() throws URISyntaxException {
        FetchCommandBuilder builder = getCommandBuilder();
        URI uri = builder.buildURI();
        String apiKey = "";
        try {
            apiKey = ApiKeyLoader.getApiKey("config.properties", "api.key");
        } catch (IOException e) {
            throw new ApiSecurityException("Unable to find API key!");
        }

        return HttpRequest
                .newBuilder()
                .header("X-Api-Key", apiKey)
                .uri(uri)
                .build();
    }

    private void writeToClient(String message) throws IOException {
        buffer.clear();
        buffer.put(message.getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        client.write(buffer);
    }

    @Override
    public void execute() throws IOException {
        if (args.length == 1) {
            throw new InvalidCommandException("Fetch command should contain at least 1 criterion");
        }

        try {
            HttpRequest request = createRequest();
            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            writeToClient(response.body());
        } catch (URISyntaxException | InterruptedException e) {
            writeToClient("Unexpected server error!");
        } catch (InvalidCommandException e) {
            writeToClient(e.getMessage());
        }
    }
}
