package bg.sofia.uni.fmi.mjt.newsfeed;

import bg.sofia.uni.fmi.mjt.newsfeed.criteria.FilterKey;
import bg.sofia.uni.fmi.mjt.newsfeed.response.pagination.PaginatedNewsList;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LimitedRateException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.NewsFeedResponseException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.ParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.SourcesException;

import java.util.Map;

public interface NewsFeedApi {
    PaginatedNewsList searchByCriteria(Map<FilterKey, String> criteria, int resultsPerPage)  throws
            LimitedRateException, ParameterException, SourcesException, NewsFeedResponseException;
}
