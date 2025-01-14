package bg.sofia.uni.fmi.mjt.poll.server.response;

public class Response {
    private StatusCode code;
    private String messageKey;
    private String messageVal;

    public Response(StatusCode code, String messageKey, String messageVal) {
        this.code = code;
        this.messageKey = messageKey;
        this.messageVal = messageVal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb
                .append("{\"status\":\"")
                .append(code.toString())
                .append("\",\"")
                .append(messageKey)
                .append("\":")
                .append(messageVal)
                .append('}');
        return sb.toString();
    }
}
