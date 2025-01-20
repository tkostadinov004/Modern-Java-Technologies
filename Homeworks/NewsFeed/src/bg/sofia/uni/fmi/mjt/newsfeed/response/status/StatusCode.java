package bg.sofia.uni.fmi.mjt.newsfeed.response.status;

public enum StatusCode {
    OK(Constants.OK_CODE),
    BAD_REQUEST(Constants.BAD_REQUEST_CODE),
    UNAUTHORIZED(Constants.UNAUTHORIZED_CODE),
    TOO_MANY_REQUESTS(Constants.TOO_MANY_REQUESTS_CODE),
    SERVER_ERROR(Constants.SERVER_ERROR_CODE);

    private static class Constants {
        private static final int OK_CODE = 200;
        private static final int BAD_REQUEST_CODE = 400;
        private static final int UNAUTHORIZED_CODE = 401;
        private static final int TOO_MANY_REQUESTS_CODE = 429;
        private static final int SERVER_ERROR_CODE = 500;
    }

    private int value;
    StatusCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
