package bg.sofia.uni.fmi.mjt.newsfeed.request.criteria;

import java.net.URI;

public record FetchRequest(int resultsPerPage, int currentPage, URI uri, RequestBuilder builder) {
    public static RequestBuilder newBuilder() {
        return new FetchRequestBuilder();
    }
}
