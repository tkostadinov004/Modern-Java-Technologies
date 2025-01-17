package bg.sofia.uni.fmi.mjt.newsfeed.request;

import bg.sofia.uni.fmi.mjt.newsfeed.request.builder.FetchRequestBuilder;
import bg.sofia.uni.fmi.mjt.newsfeed.request.security.ApiKeyLoader;
import bg.sofia.uni.fmi.mjt.newsfeed.request.security.ApiSecurityException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.ResponseHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RequestSender {
    private HttpRequest createRequest(URI uri) throws URISyntaxException {
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

    public ResponseHandler sendRequest(URI uri) throws URISyntaxException, InterruptedException, IOException {
        HttpRequest request = createRequest(uri);
        HttpClient client = HttpClient.newBuilder().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new ResponseHandler(response.statusCode(), response.body());
    }

    public ResponseHandler sendRequest(FetchRequestBuilder requestBuilder) throws InterruptedException, IOException {
        try {
            HttpRequest request = createRequest(requestBuilder.buildURI());
            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new ResponseHandler(response.statusCode(), response.body());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
