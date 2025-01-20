package bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception;

public class MissingParameterException extends Exception {
    public MissingParameterException(String message) {
        super(message);
    }

    public MissingParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
