package bg.sofia.uni.fmi.mjt.newsfeed.request.security;

import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ApiKeyLoaderTest {
    private static final String API_KEY_PROPERTY = "api.key";
    private static final String API_KEY = "example-key";

    @Test
    public void getsAPIKeyCorrectly() {
        Reader reader = new StringReader(API_KEY_PROPERTY + "=" + API_KEY);
        ApiKeyLoader loader = new ApiKeyLoader(reader);

        assertEquals(API_KEY, loader.getApiKey(API_KEY_PROPERTY));
    }

    @Test
    public void loadThrowsOnNonExistingKey()  {
        Reader reader = new StringReader("jakshgdkhgasjd=bndasjgd");
        ApiKeyLoader loader = new ApiKeyLoader(reader);

        assertThrows(ApiSecurityException.class,
                () -> loader.getApiKey(API_KEY_PROPERTY));
    }
}
