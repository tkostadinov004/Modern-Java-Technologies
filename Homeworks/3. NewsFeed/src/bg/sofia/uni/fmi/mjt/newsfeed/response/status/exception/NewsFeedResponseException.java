package bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception;

public class NewsFeedResponseException extends Exception {
    public NewsFeedResponseException(String message) {
        super(message);
    }

    public NewsFeedResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}

