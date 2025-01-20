package bg.sofia.uni.fmi.mjt.newsfeed.response;

import bg.sofia.uni.fmi.mjt.newsfeed.news.NewsArticle;
import bg.sofia.uni.fmi.mjt.newsfeed.news.NewsSource;
import bg.sofia.uni.fmi.mjt.newsfeed.response.pagination.NewsResponsePage;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LimitedRateException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LogicalParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.MissingParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.NewsFeedResponseException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.SourcesException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

public class ResponseHandlerTest {
    @Test
    public void deserializePageThrowsOnInvalidParameter() {
        ResponseHandler handler =
                new ResponseHandler(400, null, "{\"code\": \"parameterInvalid\"}");

        assertThrows(LogicalParameterException.class,
                () -> handler.deserializePage(),
                "Exception should be thrown when having an invalid parameter");
    }

    @Test
    public void deserializePageThrowsOnMissingParameter() {
        ResponseHandler handler =
                new ResponseHandler(400, null, "{\"code\": \"parametersMissing\"}");

        assertThrows(MissingParameterException.class,
                () -> handler.deserializePage(),
                "Exception should be thrown when having missing parameters");
    }

    @Test
    public void deserializePageThrowsOnExceedingRateLimit() {
        ResponseHandler handler =
                new ResponseHandler(400, null,"{\"code\": \"rateLimited\"}");

        assertThrows(LimitedRateException.class,
                () -> handler.deserializePage(),
                "Exception should be thrown when exceeding the rate limit");
    }

    @Test
    public void deserializePageThrowsOnTooManySources() {
        ResponseHandler handler =
                new ResponseHandler(400, null,"{\"code\": \"sourcesTooMany\"}");

        assertThrows(SourcesException.class,
                () -> handler.deserializePage(),
                "Exception should be thrown when sources are too many");
    }

    @Test
    public void deserializePageThrowsOnNonExistingSource() {
        ResponseHandler handler =
                new ResponseHandler(400, null,"{\"code\": \"sourceDoesNotExist\"}");

        assertThrows(SourcesException.class,
                () -> handler.deserializePage(),
                "Exception should be thrown when a source does not exist");
    }

    @Test
    public void deserializePageThrowsDefaultExceptionInOtherCases() {
        ResponseHandler handler =
                new ResponseHandler(400, null,"{\"code\": \"exampleErrorCode\"}");

        assertThrows(NewsFeedResponseException.class,
                () -> handler.deserializePage(),
                "Exception should be thrown when encountering a request error");
    }

    @Test
    public void deserializesCorrectly() throws LimitedRateException, LogicalParameterException,
            MissingParameterException, NewsFeedResponseException, SourcesException {
        ResponseHandler handler =
                new ResponseHandler(200, null,"{\"status\":\"ok\",\"totalResults\":97,\"articles\":[{\"source\":{\"id\":\"cbc-news\",\"name\":\"CBC News\"},\"author\":\"CBC News\",\"title\":\"Teasing a Liberal leadership bid, Mark Carney talks change, economy with The Daily Show | CBC News\",\"description\":\"Mark Carney has appeared on The Daily Show with Jon Stewart to talk about what kind of a Liberal leadership candidate he would make, Conservative Leader Pierre Poilievre and the economic threat Canada faces from the incoming Trump administration.\",\"url\":\"http://www.cbc.ca/news/politics/mark-carney-jon-stewart-daily-show-1.7430594\",\"urlToImage\":\"https://i.cbc.ca/1.7430671.1736868400!/fileImage/httpImage/image.jpg_gen/derivatives/16x9_1180/mark-carney-jon-stewart.jpg?im=Resize%3D620\",\"publishedAt\":\"2025-01-16T17:52:24.785487Z\",\"content\":\"Mark Carney appeared on The Daily Show with Jon Stewart on Monday to talk about what kind of Liberal leadership candidate he would make, Conservative Leader Pierre Poilievre and the economic threat C… [+6272 chars]\"}]}");

        NewsArticle expectedNewsArticle = new NewsArticle(new NewsSource("cbc-news", "CBC News"),
                "CBC News",
                "Teasing a Liberal leadership bid, Mark Carney talks change, economy with The Daily Show | CBC News",
                "Mark Carney has appeared on The Daily Show with Jon Stewart to talk about what kind of a Liberal leadership candidate he would make, Conservative Leader Pierre Poilievre and the economic threat Canada faces from the incoming Trump administration.",
                "http://www.cbc.ca/news/politics/mark-carney-jon-stewart-daily-show-1.7430594",
                "https://i.cbc.ca/1.7430671.1736868400!/fileImage/httpImage/image.jpg_gen/derivatives/16x9_1180/mark-carney-jon-stewart.jpg?im=Resize%3D620",
                Date.from(Instant.parse("2025-01-16T17:52:24.785487Z")),
                "Mark Carney appeared on The Daily Show with Jon Stewart on Monday to talk about what kind of Liberal leadership candidate he would make, Conservative Leader Pierre Poilievre and the economic threat C… [+6272 chars]");

        NewsResponsePage expected = new NewsResponsePage(97, new LinkedHashSet<>(List.of(expectedNewsArticle)));
        NewsResponsePage actual = handler.deserializePage();

        assertEquals(expected, actual,
                "Response handler should deserialize page from JSON successfully");
    }
}
