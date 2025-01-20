package bg.sofia.uni.fmi.mjt.newsfeed.response.pagination;

import java.util.Set;

/**
 * Represents a page in a paginated list
 */
public interface Page<T> {
    Set<T> getPageData();

    int getTotalResults();
}
