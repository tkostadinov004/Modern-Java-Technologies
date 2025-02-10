package bg.sofia.uni.fmi.mjt.newsfeed.response.pagination;

import bg.sofia.uni.fmi.mjt.newsfeed.news.NewsArticle;

import java.util.Objects;
import java.util.Set;

public class NewsResponsePage implements Page<NewsArticle> {
    private final int totalResults;
    private final Set<NewsArticle> articles;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewsResponsePage that = (NewsResponsePage) o;
        return totalResults == that.totalResults && Objects.equals(articles, that.articles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalResults, articles);
    }

    public NewsResponsePage(int totalResults, Set<NewsArticle> articles) {
        this.totalResults = totalResults;
        this.articles = articles;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public Set<NewsArticle> getPageData() {
        return articles;
    }
}
