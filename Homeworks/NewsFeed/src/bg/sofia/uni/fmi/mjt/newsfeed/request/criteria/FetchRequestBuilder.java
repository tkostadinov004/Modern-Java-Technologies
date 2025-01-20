package bg.sofia.uni.fmi.mjt.newsfeed.request.criteria;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class FetchRequestBuilder implements RequestBuilder {
    private Set<String> sources;
    private Set<String> keywords;
    private String country;
    private NewsCategory category;
    private int pageSize;
    private int page;

    private static final String API_ENDPOINT = "https://newsapi.org/v2/top-headlines?";

    public FetchRequestBuilder() {
        this.sources = new HashSet<>();
        this.keywords = new HashSet<>();
        this.country = null;
        this.category = null;
        this.pageSize = -1;
        this.page = -1;
    }

    public FetchRequestBuilder sources(Set<String> sources) {
        if (sources == null || sources.isEmpty()) {
            throw new IllegalArgumentException("Sources set cannot be null or empty!");
        }

        this.sources = new HashSet<>(sources);
        return this;
    }

    public FetchRequestBuilder keywords(Set<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            throw new IllegalArgumentException("Keywords set cannot be null or empty!");
        }

        this.keywords = new HashSet<>(keywords);
        return this;
    }

    public FetchRequestBuilder country(String country) {
        if (country == null || country.isEmpty() || country.isBlank()) {
            throw new IllegalArgumentException("Country cannot be null, empty, or blank!");
        }

        this.country = country;
        return this;
    }

    public FetchRequestBuilder category(NewsCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null!");
        }

        this.category = category;
        return this;
    }

    public FetchRequestBuilder paginate(int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size cannot be less than or equal to 0");
        }

        this.pageSize = pageSize;
        return this;
    }

    public FetchRequestBuilder page(int page) {
        if (page <= 0) {
            throw new IllegalArgumentException("Page number cannot be less than or equal to 0");
        }

        this.page = page;
        return this;
    }

    private URI buildURI() throws URISyntaxException {
        StringBuilder sb = new StringBuilder(API_ENDPOINT);
        if (!keywords.isEmpty()) {
            sb.append("q=").append(String.join("+", keywords)).append("&");
        }
        if (!sources.isEmpty()) {
            sb.append("sources=").append(String.join(",", sources)).append("&");
        }
        if (category != null) {
            sb.append("category=").append(category).append("&");
        }
        if (country != null) {
            sb.append("country=").append(country).append("&");
        }
        if (pageSize != -1) {
            sb.append("pageSize=").append(pageSize).append("&");
        }
        if (page != -1) {
            sb.append("page=").append(page);
        }

        String uriString = sb.toString();
        if (uriString.endsWith("&")) {
            uriString = uriString.substring(0, uriString.length() - 1);
        }
        return new URI(uriString);
    }

    public FetchRequest build() {
        try {
            return new FetchRequest(pageSize, page, buildURI(), this);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
