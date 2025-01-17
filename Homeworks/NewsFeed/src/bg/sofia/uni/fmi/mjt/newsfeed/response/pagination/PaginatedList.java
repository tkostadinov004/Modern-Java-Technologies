package bg.sofia.uni.fmi.mjt.newsfeed.response.pagination;

public interface PaginatedList<T> {
    int getPagesCount();

    boolean hasNextPage();

    boolean hasPreviousPage();

    Page<T> nextPage();

    Page<T> previousPage();
}
