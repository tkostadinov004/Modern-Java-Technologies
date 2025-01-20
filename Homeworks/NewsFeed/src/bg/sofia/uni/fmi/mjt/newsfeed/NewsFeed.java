package bg.sofia.uni.fmi.mjt.newsfeed;

import bg.sofia.uni.fmi.mjt.newsfeed.request.criteria.FetchRequest;
import bg.sofia.uni.fmi.mjt.newsfeed.news.NewsArticle;
import bg.sofia.uni.fmi.mjt.newsfeed.request.RequestSender;
import bg.sofia.uni.fmi.mjt.newsfeed.response.pagination.Page;
import bg.sofia.uni.fmi.mjt.newsfeed.response.pagination.PaginatedNewsList;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LimitedRateException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LogicalParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.MissingParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.NewsFeedResponseException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.SourcesException;

public class NewsFeed implements NewsFeedApi {
    private RequestSender requestSender;

    public NewsFeed(RequestSender requestSender) {
        this.requestSender = requestSender;
    }

    public PaginatedNewsList getNewsByRequestObject(FetchRequest request) throws
            MissingParameterException, LogicalParameterException, SourcesException,
            LimitedRateException, NewsFeedResponseException {
        Page<NewsArticle> initialPage =
                    requestSender.sendRequest(request.uri()).deserializePage();

        int resultsPerPage =
                    request.resultsPerPage() == -1 ? initialPage.getTotalResults() : request.resultsPerPage();
        return new PaginatedNewsList(resultsPerPage,
                    request.currentPage(),
                    initialPage,
                    request.builder(),
                    requestSender);
    }
}