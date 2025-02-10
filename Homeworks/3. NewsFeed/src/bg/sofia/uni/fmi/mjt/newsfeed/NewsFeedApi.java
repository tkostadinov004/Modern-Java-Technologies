package bg.sofia.uni.fmi.mjt.newsfeed;

import bg.sofia.uni.fmi.mjt.newsfeed.request.criteria.FetchRequest;
import bg.sofia.uni.fmi.mjt.newsfeed.response.pagination.PaginatedNewsList;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LimitedRateException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LogicalParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.MissingParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.NewsFeedResponseException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.SourcesException;

public interface NewsFeedApi {
    /**
     * Returns a {@code PaginatedNewsList} of news articles, filtered by the outlined criteria.
     *
     * @param request the request object that will be used for querying
     * @return {@code PaginatedNewsList} of news articles, filtered by the outlined criteria
     * @throws LimitedRateException if API limit is reached
     * @throws MissingParameterException if one or more required parameters are missing
     * @throws LogicalParameterException if one or more of the parameters have invalid values
     * @throws SourcesException if the specified sources are too many, or at least one of them doesn't exist
     * @throws NewsFeedResponseException if any other logical error in handling the request occurs
     */
    PaginatedNewsList getNewsByRequestObject(FetchRequest request) throws
            MissingParameterException, LogicalParameterException, SourcesException,
            LimitedRateException, NewsFeedResponseException;
}
