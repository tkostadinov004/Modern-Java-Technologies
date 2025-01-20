package bg.sofia.uni.fmi.mjt.newsfeed.request.security;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

public class ApiKeyLoader {
    private Reader reader;

    public ApiKeyLoader(Reader reader) {
        this.reader = reader;
    }

    public String getApiKey(String propertyKeyName) {
        Properties properties = new Properties();
        try {
            properties.load(reader);
        } catch (IOException e) {
            throw new ApiSecurityException("Unable to find API key!", e);
        }

        String key = properties.getProperty(propertyKeyName);
        if (key == null) {
            throw new ApiSecurityException("Unable to find API key!");
        }
        return key;
    }
}
