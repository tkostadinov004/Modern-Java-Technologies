package bg.sofia.uni.fmi.mjt.newsfeed.server.request.command.builder;

import bg.sofia.uni.fmi.mjt.newsfeed.server.request.command.exception.InvalidCommandException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FetchCommandBuilder {
    private Set<String> sources;
    private String keyword;
    private String country;
    private String category;
    private int pageSize;

    public FetchCommandBuilder() {
        this.sources = new HashSet<>();
        this.keyword = this.country = this.category = null;
        this.pageSize = -1;
    }

    public FetchCommandBuilder filterSources(String sources) {
        if (this.country != null || this.category != null) {
            throw new InvalidCommandException("You can't mix this param with the country or category params.");
        }

        this.sources = new HashSet<>(Arrays.stream(sources.split(",")).toList());
        return this;
    }

    public FetchCommandBuilder filterByKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public FetchCommandBuilder filterCountry(String country) {
        if (!this.sources.isEmpty()) {
            throw new InvalidCommandException("You can't mix this param with the sources param.");
        }

        this.country = country;
        return this;
    }

    public FetchCommandBuilder filterCategory(String category) {
        if (!this.sources.isEmpty()) {
            throw new InvalidCommandException("You can't mix this param with the sources param.");
        }

        this.category = category;
        return this;
    }

    public FetchCommandBuilder paginate(int pageSize) {
        this.pageSize = pageSize;
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
            sb.append("pageSize=").append(pageSize);
        }

        return new URI(sb.toString());
    }
}
