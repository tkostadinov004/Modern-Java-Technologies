package bg.sofia.uni.fmi.mjt.newsfeed;

import bg.sofia.uni.fmi.mjt.newsfeed.news.NewsArticle;
import bg.sofia.uni.fmi.mjt.newsfeed.news.NewsSource;
import bg.sofia.uni.fmi.mjt.newsfeed.request.RequestSender;
import bg.sofia.uni.fmi.mjt.newsfeed.request.criteria.FetchRequest;
import bg.sofia.uni.fmi.mjt.newsfeed.request.criteria.FetchRequestBuilder;
import bg.sofia.uni.fmi.mjt.newsfeed.response.ResponseHandler;
import bg.sofia.uni.fmi.mjt.newsfeed.response.pagination.NewsResponsePage;
import bg.sofia.uni.fmi.mjt.newsfeed.response.pagination.PaginatedList;
import bg.sofia.uni.fmi.mjt.newsfeed.response.pagination.PaginatedNewsList;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LimitedRateException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LogicalParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.MissingParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.NewsFeedResponseException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.SourcesException;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NewsFeedTest {
    private final RequestSender requestSender = mock();

    private Set<NewsArticle> getSampleArticles() {
        NewsSource source1 = new NewsSource("fox-news", "Fox News");
        String author1 = "Hanna Panreck";
        String title1 = "ABC host tells 'The View' Trump 'clearly had an effect' in securing cease-fire deal";
        String description1 = "ABC News host Martha Raddatz said Wednesday on \"The View\" that President-elect Trump \"clearly had an effect\" on the cease-fire deal reached between Israel and Hamas.";
        String url1 = "https://www.foxnews.com/media/abc-host-tells-the-view-trump-clearly-had-effect-securing-cease-fire-deal";
        String urlToImage1 = "https://static.foxnews.com/foxnews.com/content/uploads/2025/01/martha-raddatz-the-view.jpg";
        Date publishedAt1 = Date.from(Instant.parse(("2025-01-16T19:26:39Z")));
        String content1 = "ABC News host Martha Raddatz joined the co-hosts of \"The View\" on Thursday and said that President-elect Trump's team \"clearly had an effect\" on the cease-fire deal between Israel and Hamas.\r\n\"Well, … [+2934 chars]";

        NewsArticle article1 = new NewsArticle(source1, author1, title1, description1, url1, urlToImage1, publishedAt1, content1);

        NewsSource source2 = new NewsSource("fox-news", "Fox News");
        String author2 = "Adam Shaw";
        String title2 = "Bondi spars with Schiff at testy confirmation hearing: 'You were censured'";
        String description2 = "Sen. Adam Schiff, D-Calif., grilled Pam Bondi on how she would act in terms of President-elect Trump's political opponents, leading to a fiery response from Bondi.";
        String url2 = "https://www.foxnews.com/politics/bondi-spars-schiff-testy-confirmation-hearing-you-were-censured";
        String urlToImage2 = "https://static.foxnews.com/foxnews.com/content/uploads/2025/01/schiff-bondi.jpg";
        Date publishedAt2 =  Date.from(Instant.parse("2025-01-16T19:20:33Z"));
        String content2 = "Pam Bondi, President-elect Trump's nominee to lead the Department of Justice, was involved in a sharp clash with Sen. Adam Schiff, D-Calif., on Wednesday as the California senator quizzed Bondi ove… [+2632 chars]";

        NewsArticle article2 = new NewsArticle(source2, author2, title2, description2, url2, urlToImage2, publishedAt2, content2);

        return new LinkedHashSet<>(List.of(article1, article2));
    }

    @Test
    public void getsNewsByRequestSuccessfully() throws MissingParameterException, LogicalParameterException, SourcesException,
            LimitedRateException, NewsFeedResponseException {
        HttpRequest httpRequest = mock();
        when(httpRequest.uri()).thenReturn(URI.create("test-uri"));

        FetchRequest request = new FetchRequest(2,0, URI.create("test-uri"), new FetchRequestBuilder());
        when(requestSender.sendRequest(request.uri()))
                .thenReturn(new ResponseHandler(200, httpRequest, "{\"status\":\"ok\",\"totalResults\":2,\"articles\":[{\"source\":{\"id\":\"fox-news\",\"name\":\"Fox News\"},\"author\":\"Hanna Panreck\",\"title\":\"ABC host tells 'The View' Trump 'clearly had an effect' in securing cease-fire deal\",\"description\":\"ABC News host Martha Raddatz said Wednesday on \\\"The View\\\" that President-elect Trump \\\"clearly had an effect\\\" on the cease-fire deal reached between Israel and Hamas.\",\"url\":\"https://www.foxnews.com/media/abc-host-tells-the-view-trump-clearly-had-effect-securing-cease-fire-deal\",\"urlToImage\":\"https://static.foxnews.com/foxnews.com/content/uploads/2025/01/martha-raddatz-the-view.jpg\",\"publishedAt\":\"2025-01-16T19:26:39Z\",\"content\":\"ABC News host Martha Raddatz joined the co-hosts of \\\"The View\\\" on Thursday and said that President-elect Trump's team \\\"clearly had an effect\\\" on the cease-fire deal between Israel and Hamas.\\r\\n\\\"Well, … [+2934 chars]\"},{\"source\":{\"id\":\"fox-news\",\"name\":\"Fox News\"},\"author\":\"Adam Shaw\",\"title\":\"Bondi spars with Schiff at testy confirmation hearing: 'You were censured'\",\"description\":\"Sen. Adam Schiff, D-Calif., grilled Pam Bondi on how she would act in terms of President-elect Trump's political opponents, leading to a fiery response from Bondi.\",\"url\":\"https://www.foxnews.com/politics/bondi-spars-schiff-testy-confirmation-hearing-you-were-censured\",\"urlToImage\":\"https://static.foxnews.com/foxnews.com/content/uploads/2025/01/schiff-bondi.jpg\",\"publishedAt\":\"2025-01-16T19:20:33Z\",\"content\":\"Pam Bondi, President-elect Trump's nominee to lead the Department of Justice, was involved in a sharp clash with Sen. Adam Schiff, D-Calif., on Wednesday as the California senator quizzed Bondi ove… [+2632 chars]\"}]}"));

        NewsFeedApi newsFeed = new NewsFeed(requestSender);
        PaginatedList<NewsArticle> expectedResult = new PaginatedNewsList(2,0,new NewsResponsePage(2, getSampleArticles()),new FetchRequestBuilder(), requestSender);
        PaginatedList<NewsArticle> actualResult = newsFeed.getNewsByRequestObject(request);

        assertEquals(expectedResult.getPagesCount(), actualResult.getPagesCount(),
                "Expected and actual pages amounts should be the same.");

        while(expectedResult.hasNextPage() && actualResult.hasNextPage()) {
            var expectedCurr = expectedResult.nextPage();
            var actualCurr = actualResult.nextPage();

            assertEquals(expectedCurr.getPageData().size(),
                    actualCurr.getPageData().size(),
                    "List pagination should work.");
            assertEquals(expectedCurr, actualCurr,
                    "Expected and actual pages should contain the same articles.");
        }
    }
}
