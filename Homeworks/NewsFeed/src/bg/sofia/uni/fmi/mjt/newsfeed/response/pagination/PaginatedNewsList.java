package bg.sofia.uni.fmi.mjt.newsfeed.response.pagination;

import bg.sofia.uni.fmi.mjt.newsfeed.request.criteria.FetchRequest;
import bg.sofia.uni.fmi.mjt.newsfeed.request.criteria.RequestBuilder;
import bg.sofia.uni.fmi.mjt.newsfeed.news.NewsArticle;
import bg.sofia.uni.fmi.mjt.newsfeed.request.RequestSender;
import bg.sofia.uni.fmi.mjt.newsfeed.response.ResponseHandler;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LimitedRateException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LogicalParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.NewsFeedResponseException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.MissingParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.SourcesException;

public class PaginatedNewsList implements PaginatedList<NewsArticle> {
    private int currentPageNumber;
    private final int totalPages;
    private RequestBuilder requestBuilder;
    private RequestSender requestSender;
    private Page<NewsArticle> initialPage;
    private boolean isInitialPageUsed;

    public PaginatedNewsList(int resultsPerPage,
                             int currentPage,
                             Page<NewsArticle> initialPage,
                             RequestBuilder requestBuilder,
                             RequestSender requestSender) {
        this.currentPageNumber = currentPage;
        this.totalPages = (int)Math.ceil(initialPage.getTotalResults() * 1.0 / resultsPerPage);
        this.initialPage = initialPage;
        this.requestBuilder = requestBuilder;
        this.requestSender = requestSender;
        this.isInitialPageUsed = false;
    }

    public int getPagesCount() {
        return totalPages;
    }

    private Page<NewsArticle> sendRequest(int pageNumber) throws
            NewsFeedResponseException,
            LimitedRateException, MissingParameterException, LogicalParameterException, SourcesException {
        if (pageNumber > 0) {
            requestBuilder.page(pageNumber);
        }
        FetchRequest request = requestBuilder.build();
        ResponseHandler response = requestSender.sendRequest(request.uri());
        return response.deserializePage();
    }

    @Override
    public boolean hasNextPage() {
        return currentPageNumber < totalPages;
    }

    @Override
    public boolean hasPreviousPage() {
        if (isInitialPageUsed)
            return currentPageNumber > 1;
        return currentPageNumber > 0;
    }

    @Override
    public Page<NewsArticle> nextPage() throws
            NewsFeedResponseException,
            LimitedRateException, MissingParameterException, LogicalParameterException, SourcesException {
        if (!hasNextPage()) {
            throw new PaginationException("There is no next page!");
        }
        if (currentPageNumber == 0) {
            currentPageNumber++;
            this.isInitialPageUsed = true;
            return initialPage;
        }

        return sendRequest(++currentPageNumber);
    }

    @Override
    public Page<NewsArticle> previousPage() throws
            NewsFeedResponseException,
            LimitedRateException, MissingParameterException, LogicalParameterException, SourcesException {
        if (!hasPreviousPage()) {
            throw new PaginationException("There is no previous page!");
        }

        return sendRequest(--currentPageNumber);
    }
}
