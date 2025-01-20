package bg.sofia.uni.fmi.mjt.newsfeed.request;

import bg.sofia.uni.fmi.mjt.newsfeed.request.security.ApiKeyLoader;
import bg.sofia.uni.fmi.mjt.newsfeed.request.security.ApiSecurityException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.ResponseHandler;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.NewsFeedResponseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RequestSenderTest {
    private final String API_KEY_PROPERTY = "api.key";
    private final String API_KEY = "example-key";
    private final Reader API_KEY_READER = new StringReader(API_KEY_PROPERTY+"="+API_KEY);

    @Test
    public void requestSendingFailsOnInvalidApiKey() {
        RequestSender sender = new RequestSender(new StringReader(""),
                "", HttpClient.newBuilder().build());

        assertThrows(ApiSecurityException.class,
                () -> sender.sendRequest(URI.create("https://google.com/")),
                "Request sender should throw when API key is not present.");
    }

    @Test
    public void requestSendingThrowsWithInvalidURI() {
        RequestSender sender = new RequestSender(API_KEY_READER,
                API_KEY_PROPERTY, HttpClient.newBuilder().build());

        assertThrows(IllegalArgumentException.class,
                () -> sender.sendRequest(new URI("")));
    }

    @Test
    public void requestSendingSendsRequest() throws IOException, InterruptedException, NewsFeedResponseException {
        HttpRequest httpRequest = HttpRequest
                .newBuilder()
                .header("X-Api-Key", API_KEY)
                .uri(URI.create("https://google.com/"))
                .build();

        HttpResponse<String> httpResponse = mock();
        when(httpResponse.statusCode())
                .thenReturn(200);
        when(httpResponse.body())
                .thenReturn("body example");

        HttpClient httpClient = mock();
        when(httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()))
                .thenReturn(httpResponse);

        RequestSender sender = new RequestSender(API_KEY_READER, API_KEY_PROPERTY, httpClient);
        ResponseHandler actual = sender.sendRequest(URI.create("https://google.com/"));
        ResponseHandler expected = new ResponseHandler(200,  httpRequest, "body example");

        assertEquals(expected.getStatusCode(), actual.getStatusCode());
        assertEquals(expected.getRequest(), actual.getRequest());
        assertEquals(expected.getBody(), actual.getBody());

        verify(httpClient, times(1))
                .send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }
}
