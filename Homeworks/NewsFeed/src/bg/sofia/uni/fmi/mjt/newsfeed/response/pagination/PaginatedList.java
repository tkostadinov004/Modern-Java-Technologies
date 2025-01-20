package bg.sofia.uni.fmi.mjt.newsfeed.response.pagination;

import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LimitedRateException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LogicalParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.MissingParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.NewsFeedResponseException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.SourcesException;

public interface PaginatedList<T> {
    /**
     * @return the amount of pages in the list
     */
    int getPagesCount();

    /**
     * Checks whether a next page exists.
     *
     * @return {@code true} if a next page exists, {@code false} otherwise
     */
    boolean hasNextPage();

    /**
     * Checks whether a previous page exists.
     *
     * @return {@code true} if a previous page exists, {@code false} otherwise
     */
    boolean hasPreviousPage();

    /**
     * Returns the next page in the paginated list.
     *
     * @return the next page in the paginated list
     * @throws PaginationException if there is no next page
     * @throws LimitedRateException if API limit is reached
     * @throws MissingParameterException if one or more required parameters are missing
     * @throws LogicalParameterException if one or more of the parameters have invalid values
     * @throws SourcesException if the specified sources are too many, or at least one of them doesn't exist
     * @throws NewsFeedResponseException if any other logical error in handling the request occurs
     */
    Page<T> nextPage() throws
            NewsFeedResponseException,
            LimitedRateException, MissingParameterException, LogicalParameterException, SourcesException;

    /**
     * Returns the previous page in the paginated list.
     *
     * @return the previous page in the paginated list
     * @throws PaginationException if there is no previous page
     * @throws LimitedRateException if API limit is reached
     * @throws MissingParameterException if one or more required parameters are missing
     * @throws LogicalParameterException if one or more of the parameters have invalid values
     * @throws SourcesException if the specified sources are too many, or at least one of them doesn't exist
     * @throws NewsFeedResponseException if any other logical error in handling the request occurs
     */
    Page<T> previousPage() throws
            NewsFeedResponseException,
            LimitedRateException, MissingParameterException, LogicalParameterException, SourcesException;
}
