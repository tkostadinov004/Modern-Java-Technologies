package bg.sofia.uni.fmi.mjt.newsfeed.response.pagination;

public class PaginationException extends RuntimeException {
    public PaginationException(String message) {
        super(message);
    }

    public PaginationException(String message, Throwable cause) {
        super(message, cause);
    }
}
