package bg.sofia.uni.fmi.mjt.newsfeed.server.request.secure;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ApiKeyLoader {
    public static String getApiKey(String filename, String propertyKeyName) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(filename)) {
            properties.load(input);
        }
        return properties.getProperty(propertyKeyName);
    }
}
