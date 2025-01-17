package bg.sofia.uni.fmi.mjt.newsfeed.response.pagination;

import bg.sofia.uni.fmi.mjt.newsfeed.news.NewsArticle;
import bg.sofia.uni.fmi.mjt.newsfeed.request.RequestSender;
import bg.sofia.uni.fmi.mjt.newsfeed.request.builder.FetchRequestBuilder;
import bg.sofia.uni.fmi.mjt.newsfeed.response.ResponseHandler;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LimitedRateException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.NewsFeedResponseException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.ParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.SourcesException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class PaginatedNewsList implements PaginatedList<NewsArticle> {
    private int currentPageNumber;
    private final int totalPages;
    private FetchRequestBuilder requestBuilder;
    private RequestSender requestSender;
    private Page<NewsArticle> initialPage;

    public PaginatedNewsList(int resultsPerPage,
                             Page<NewsArticle> initialPage,
                             FetchRequestBuilder requestBuilder,
                             RequestSender requestSender) {
        this.currentPageNumber = 0;
        this.totalPages = (int)Math.ceil(initialPage.getTotalResults() * 1.0 / resultsPerPage);
        this.initialPage = initialPage;
        this.requestBuilder = requestBuilder;
        this.requestSender = requestSender;
    }

    public int getPagesCount() {
        return totalPages;
    }

    private Page<NewsArticle> sendRequest(int pageNumber) throws
            URISyntaxException, IOException,
            InterruptedException, NewsFeedResponseException,
            LimitedRateException, ParameterException, SourcesException {
        if (pageNumber > 0) {
            requestBuilder.page(pageNumber);
        }
        URI uri = requestBuilder.buildURI();
        ResponseHandler response = requestSender.sendRequest(uri);
        return response.deserializePage();
    }

    @Override
    public boolean hasNextPage() {
        return currentPageNumber < totalPages;
    }

    @Override
    public boolean hasPreviousPage() {
        return currentPageNumber > 1;
    }

    @Override
    public Page<NewsArticle> nextPage() {
        if (!hasNextPage()) {
            throw new PaginationException("There is no next page!");
        }
        if (currentPageNumber == 0) {
            currentPageNumber++;
            return initialPage;
        }

        try {
            return sendRequest(++currentPageNumber);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<NewsArticle> previousPage() {
        if (!hasPreviousPage()) {
            throw new PaginationException("There is no previous page!");
        }

        try {
            return sendRequest(--currentPageNumber);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
