package bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception;

public class SourcesException extends Exception {
    public SourcesException(String message) {
        super(message);
    }

    public SourcesException(String message, Throwable cause) {
        super(message, cause);
    }
}
