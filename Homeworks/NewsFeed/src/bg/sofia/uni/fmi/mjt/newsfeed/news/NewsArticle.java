package bg.sofia.uni.fmi.mjt.newsfeed.news;

import java.util.Date;

public record NewsArticle(NewsSource source,
        String title,
        String description,
        String url,
        String urlToImage,
        Date publishedAt,
        String content) {

    @Override
    public String toString() {
        return description;
    }
}
