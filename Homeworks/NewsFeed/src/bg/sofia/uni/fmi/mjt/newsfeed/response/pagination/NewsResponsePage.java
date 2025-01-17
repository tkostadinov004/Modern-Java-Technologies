package bg.sofia.uni.fmi.mjt.newsfeed.response.pagination;

import bg.sofia.uni.fmi.mjt.newsfeed.news.NewsArticle;

import java.util.Set;

public class NewsResponsePage implements Page<NewsArticle> {
    private int totalResults;
    private Set<NewsArticle> articles;

    public NewsResponsePage(int totalResults, Set<NewsArticle> articles) {
        this.totalResults = totalResults;
        this.articles = articles;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public Set<NewsArticle> getPageData() {
        return articles;
    }

    public void setArticles(Set<NewsArticle> articles) {
        this.articles = articles;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (var article : articles) {
            sb.append(article);
            sb.append(System.lineSeparator());
        }
        return sb.toString().stripTrailing();
    }
}
