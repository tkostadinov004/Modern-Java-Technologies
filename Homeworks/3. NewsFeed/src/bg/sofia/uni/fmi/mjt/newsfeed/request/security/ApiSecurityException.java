package bg.sofia.uni.fmi.mjt.newsfeed.request.security;

public class ApiSecurityException extends RuntimeException {
    public ApiSecurityException(String message) {
        super(message);
    }

    public ApiSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
