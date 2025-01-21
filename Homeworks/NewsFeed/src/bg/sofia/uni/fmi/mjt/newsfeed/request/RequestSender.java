package bg.sofia.uni.fmi.mjt.newsfeed.request;

import bg.sofia.uni.fmi.mjt.newsfeed.request.security.ApiKeyLoader;
import bg.sofia.uni.fmi.mjt.newsfeed.response.ResponseHandler;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.NewsFeedResponseException;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RequestSender {
    private final Reader propertiesReader;
    private final String apiPropertyKeyName;
    private final HttpClient client;

    public RequestSender(Reader propertiesReader, String apiPropertyKeyName, HttpClient client) {
        this.propertiesReader = propertiesReader;
        this.apiPropertyKeyName = apiPropertyKeyName;
        this.client = client;
    }

    private HttpRequest createRequest(URI uri) {
        ApiKeyLoader loader = new ApiKeyLoader(propertiesReader);
        String apiKey = loader.getApiKey(apiPropertyKeyName);

        return HttpRequest
                .newBuilder()
                .header("X-Api-Key", apiKey)
                .uri(uri)
                .build();
    }

    public ResponseHandler sendRequest(URI uri) throws NewsFeedResponseException {
        try {
            HttpRequest request = createRequest(uri);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new ResponseHandler(response.statusCode(), request, response.body());
        } catch (IOException | InterruptedException e) {
            throw new NewsFeedResponseException(e.getMessage(), e);
        }
    }
}
