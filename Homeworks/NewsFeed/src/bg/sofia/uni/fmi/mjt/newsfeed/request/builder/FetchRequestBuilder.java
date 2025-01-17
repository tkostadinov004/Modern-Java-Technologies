package bg.sofia.uni.fmi.mjt.newsfeed.request.builder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FetchRequestBuilder {
    private Set<String> sources;
    private Set<String> keywords;
    private String country;
    private String category;
    private int pageSize;
    private int page;

    public FetchRequestBuilder() {
        this.sources = new HashSet<>();
        this.keywords = new HashSet<>();
        this.country = this.category = null;
        this.pageSize = -1;
        this.page = -1;
    }

    public FetchRequestBuilder filterSources(String sources) {
        if (this.country != null || this.category != null) {
            throw new InvalidCommandException("You can't mix this param with the country or category params.");
        }

        this.sources = new HashSet<>(Arrays.stream(sources.split(",")).toList());
        return this;
    }

    public FetchRequestBuilder filterByKeyword(String keyword) {
        this.keywords = keyword.split(",")
        return this;
    }

    public FetchRequestBuilder filterCountry(String country) {
        if (!this.sources.isEmpty()) {
            throw new InvalidCommandException("You can't mix this param with the sources param.");
        }

        this.country = country;
        return this;
    }

    public FetchRequestBuilder filterCategory(String category) {
        if (!this.sources.isEmpty()) {
            throw new InvalidCommandException("You can't mix this param with the sources param.");
        }

        this.category = category;
        return this;
    }

    public FetchRequestBuilder paginate(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public FetchRequestBuilder page(int page) {
        this.page = page;
        return this;
    }

    public URI buildURI() throws URISyntaxException {
        StringBuilder sb = new StringBuilder("https://newsapi.org/v2/top-headlines?");
        if (keyword != null) {
            sb.append("q=").append(keyword).append("&");
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
}
