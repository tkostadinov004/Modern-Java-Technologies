package bg.sofia.uni.fmi.mjt.newsfeed;

import bg.sofia.uni.fmi.mjt.newsfeed.criteria.FilterKey;
import bg.sofia.uni.fmi.mjt.newsfeed.news.NewsArticle;
import bg.sofia.uni.fmi.mjt.newsfeed.request.RequestSender;
import bg.sofia.uni.fmi.mjt.newsfeed.request.builder.FetchRequestBuilder;
import bg.sofia.uni.fmi.mjt.newsfeed.response.pagination.PaginatedNewsList;
import bg.sofia.uni.fmi.mjt.newsfeed.response.pagination.Page;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LimitedRateException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.ParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.SourcesException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.NewsFeedResponseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

public class NewsFeed implements NewsFeedApi {
    @Override
    public PaginatedNewsList searchByCriteria(Map<FilterKey, String> criteria, int resultsPerPage) throws
            LimitedRateException, ParameterException, SourcesException, NewsFeedResponseException {
        FetchRequestBuilder requestBuilder = new FetchRequestBuilder();
        for (var entry : criteria.entrySet()) {
            switch(entry.getKey()) {
                case KEYWORD -> requestBuilder.filterByKeyword(entry.getValue());
                case COUNTRY -> requestBuilder.filterCountry(entry.getValue());
                case CATEGORY -> requestBuilder.filterCategory(entry.getValue());
                case SOURCES -> requestBuilder.filterSources(entry.getValue());
            }
        }
        requestBuilder.paginate(resultsPerPage);
        RequestSender sender = new RequestSender();
        try {
            Page<NewsArticle> initialPage =
                    sender.sendRequest(requestBuilder).deserializePage();

            return new PaginatedNewsList(resultsPerPage, initialPage, requestBuilder, sender);
        } catch (IOException | InterruptedException e) {
            throw new NewsFeedResponseException(e.getMessage(), e);
        }
    }
}

class Program {
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        Map<FilterKey, String> criteria = Map.of(FilterKey.KEYWORD, "trump", FilterKey.SOURCES, "cbc-news,cnn,fox-news");
        try {
            var list = new NewsFeed().searchByCriteria(criteria, 2);
            while (list.hasNextPage()) {
                System.out.println(list.nextPage() + "\n\n\n");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}