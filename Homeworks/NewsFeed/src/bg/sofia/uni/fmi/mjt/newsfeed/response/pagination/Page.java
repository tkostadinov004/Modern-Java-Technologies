package bg.sofia.uni.fmi.mjt.newsfeed.response.pagination;

import java.util.Set;

public interface Page<T> {
    Set<T> getPageData();

    int getTotalResults();
}
