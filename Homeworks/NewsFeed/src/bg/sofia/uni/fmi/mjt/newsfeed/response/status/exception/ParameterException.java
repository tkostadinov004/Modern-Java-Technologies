package bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception;

public class ParameterException extends Exception {
    public ParameterException(String message) {
        super(message);
    }

    public ParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
