package bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception;

public class LogicalParameterException extends Exception {
    public LogicalParameterException(String message) {
        super(message);
    }

    public LogicalParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
