package bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception;

public class LimitedRateException extends Exception {
    public LimitedRateException(String message) {
        super(message);
    }

    public LimitedRateException(String message, Throwable cause) {
        super(message, cause);
    }
}
