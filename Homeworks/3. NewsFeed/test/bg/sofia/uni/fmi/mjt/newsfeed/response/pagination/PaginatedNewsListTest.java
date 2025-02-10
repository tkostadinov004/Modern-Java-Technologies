package bg.sofia.uni.fmi.mjt.newsfeed.response.pagination;

import bg.sofia.uni.fmi.mjt.newsfeed.news.NewsArticle;
import bg.sofia.uni.fmi.mjt.newsfeed.request.RequestSender;
import bg.sofia.uni.fmi.mjt.newsfeed.request.criteria.FetchRequest;
import bg.sofia.uni.fmi.mjt.newsfeed.request.criteria.RequestBuilder;
import bg.sofia.uni.fmi.mjt.newsfeed.response.ResponseHandler;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LimitedRateException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LogicalParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.MissingParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.NewsFeedResponseException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.SourcesException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaginatedNewsListTest {
    private static Page<NewsArticle> initialPage;
    private static RequestBuilder requestBuilder;
    private static RequestSender requestSender;
    private static List<NewsArticle> articles;
    private static final int RESULTS_PER_PAGE = 3;
    private static final int TOTAL_RESULTS = 10;

    @BeforeAll
    public static void setUp() {
        articles = new ArrayList<>();
        for (int i = 0; i < TOTAL_RESULTS; i++) {
            articles.add(new NewsArticle(null,
                    null,
                    "title"+(i+1),
                    null,
                    null,
                    null,
                    null,
                    null));
        }

        initialPage = new NewsResponsePage(TOTAL_RESULTS,
                new LinkedHashSet<>(articles.stream().limit(RESULTS_PER_PAGE).toList()));
        requestBuilder = mock();
        requestSender = mock();
    }

    @Test
    public void nextPageThrowsIfThereIsNoNextPage() {
        PaginatedList<NewsArticle> paginatedList =
                new PaginatedNewsList(10, 0, initialPage, requestBuilder, requestSender);

        assertDoesNotThrow(paginatedList::nextPage,
                "The initial page is not used yet, therefore no exception should be thrown.");
        assertThrows(PaginationException.class,
                paginatedList::nextPage,
                "Exception should be thrown when trying to get next page after the last one.");
    }

    @Test
    public void getsNextPageCorrectly() throws
            NewsFeedResponseException,
            LimitedRateException, MissingParameterException, LogicalParameterException, SourcesException {
        PaginatedList<NewsArticle> paginatedList =
                new PaginatedNewsList(RESULTS_PER_PAGE,0, initialPage, requestBuilder, requestSender);
        int pages = (int)Math.ceil(TOTAL_RESULTS * 1.0 / RESULTS_PER_PAGE);
        String[] bodies = new String[pages];
        Integer curr = 0;
        for (int i = 0; i < pages; i++) {
            bodies[i] = "{\"status\":\"ok\",\"totalResults\":10,\"articles\":[";
            if(++curr <= TOTAL_RESULTS) {
                bodies[i] += "{\"source\":null,\"author\":null,\"title\":\"title" + curr + "\",\"description\":null,\"url\":null,\"urlToImage\":null,\"publishedAt\":null,\"content\":null}";
                if(++curr <= TOTAL_RESULTS) {
                    bodies[i] += ",{\"source\":null,\"author\":null,\"title\":\"title" + curr + "\",\"description\":null,\"url\":null,\"urlToImage\":null,\"publishedAt\":null,\"content\":null}";
                    if(++curr <= TOTAL_RESULTS) {
                        bodies[i] += ",{\"source\":null,\"author\":null,\"title\":\"title" + curr + "\",\"description\":null,\"url\":null,\"urlToImage\":null,\"publishedAt\":null,\"content\":null}";
                    }
                }
            }
            bodies[i] += "]}";
        }


        Integer iterations = 1;
        while (paginatedList.hasNextPage()) {
            Page<NewsArticle> expectedPage = new NewsResponsePage(TOTAL_RESULTS,
                    new LinkedHashSet<>(articles.stream().skip(RESULTS_PER_PAGE * (iterations - 1)).limit(RESULTS_PER_PAGE).toList()));
            when(requestBuilder.build())
                    .thenReturn(new FetchRequest(RESULTS_PER_PAGE, 0, URI.create(iterations.toString()), requestBuilder));
            when(requestSender.sendRequest(URI.create(iterations.toString())))
                    .thenReturn(new ResponseHandler(200, null, bodies[iterations - 1]));

            Page<NewsArticle> actualPage = paginatedList.nextPage();
            assertEquals(expectedPage, actualPage,
                    "Next page should contain new results.");
            iterations++;
        }
    }

    @Test
    public void getsPreviousPageCorrectly() throws
            NewsFeedResponseException,
            LimitedRateException, MissingParameterException, LogicalParameterException, SourcesException {
        PaginatedList<NewsArticle> paginatedList =
                new PaginatedNewsList(RESULTS_PER_PAGE,0, initialPage, requestBuilder, requestSender);
        int pages = (int)Math.ceil(TOTAL_RESULTS * 1.0 / RESULTS_PER_PAGE);
        String[] bodies = new String[pages];
        Integer curr = 0;
        for (int i = 0; i < pages; i++) {
            bodies[i] = "{\"status\":\"ok\",\"totalResults\":10,\"articles\":[";
            if(++curr <= TOTAL_RESULTS) {
                bodies[i] += "{\"source\":null,\"author\":null,\"title\":\"title" + curr + "\",\"description\":null,\"url\":null,\"urlToImage\":null,\"publishedAt\":null,\"content\":null}";
                if(++curr <= TOTAL_RESULTS) {
                    bodies[i] += ",{\"source\":null,\"author\":null,\"title\":\"title" + curr + "\",\"description\":null,\"url\":null,\"urlToImage\":null,\"publishedAt\":null,\"content\":null}";
                    if(++curr <= TOTAL_RESULTS) {
                        bodies[i] += ",{\"source\":null,\"author\":null,\"title\":\"title" + curr + "\",\"description\":null,\"url\":null,\"urlToImage\":null,\"publishedAt\":null,\"content\":null}";
                    }
                }
            }
            bodies[i] += "]}";
        }


        Integer iterations = 1;
        while (paginatedList.hasNextPage()) {
            when(requestBuilder.build())
                    .thenReturn(new FetchRequest(RESULTS_PER_PAGE,0, URI.create(iterations.toString()), requestBuilder));
            when(requestSender.sendRequest(URI.create(iterations.toString())))
                    .thenReturn(new ResponseHandler(200, null, bodies[iterations - 1]));
            iterations++;
            paginatedList.nextPage();
        }
        iterations--;
        while(paginatedList.hasPreviousPage()) {
            when(requestBuilder.build())
                    .thenReturn(new FetchRequest(RESULTS_PER_PAGE,0, URI.create(iterations.toString()), requestBuilder));
            Page<NewsArticle> expectedPage = new NewsResponsePage(TOTAL_RESULTS,
                    new LinkedHashSet<>(articles.stream().skip(RESULTS_PER_PAGE * (iterations - 1)).limit(RESULTS_PER_PAGE).toList()));
            Page<NewsArticle> actualPage = paginatedList.previousPage();

            assertEquals(expectedPage, actualPage,
                    "Returning to the previous page should return its results.");
            iterations--;
        }
    }

    @Test
    public void previousPageThrowsIfThereIsNoPreviousPage() {
        PaginatedList<NewsArticle> paginatedList =
                new PaginatedNewsList(2,0, initialPage, requestBuilder, requestSender);

        assertThrows(PaginationException.class,
                paginatedList::previousPage);
        assertDoesNotThrow(paginatedList::nextPage);
        assertThrows(PaginationException.class,
                paginatedList::previousPage);
    }
}
