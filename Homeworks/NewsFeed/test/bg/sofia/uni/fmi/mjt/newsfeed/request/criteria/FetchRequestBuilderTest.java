package bg.sofia.uni.fmi.mjt.newsfeed.request.criteria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FetchRequestBuilderTest {
    private static final String API_ENDPOINT = "https://newsapi.org/v2/top-headlines?";
    private RequestBuilder builder;

    @BeforeEach
    public void setUp() {
        builder = FetchRequest.newBuilder();
    }

    @Test
    public void emptyRequestHasNoArguments() {
        FetchRequest request = builder.build();
        assertEquals(API_ENDPOINT, request.uri().toString(),
                "An empty request should have no arguments.");
    }

    @Test
    public void sourcesThrowsOnNullOrEmptySourcesSet() {
        assertThrows(IllegalArgumentException.class,
                () -> builder.sources(null),
                "Builder should throw on null sources set.");

        assertThrows(IllegalArgumentException.class,
                () -> builder.sources(Set.of()),
                "Builder should throw on empty sources set.");
    }

    @Test
    public void setsSourcesCorrectly() {
        Set<String> sources = new LinkedHashSet<>();
        sources.add("cnn");
        sources.add("bbc");
        sources.add("der-spiegel");
        builder.sources(sources);

        String expected = API_ENDPOINT + "sources=" + String.join(",", sources);
        String actual = builder.build().uri().toString();

        assertEquals(expected, actual,
                "Query builder should set comma-separated sources set in the request.");
    }

    @Test
    public void keywordsThrowsOnNullOrEmptyKeywordsSet() {
        assertThrows(IllegalArgumentException.class,
                () -> builder.keywords(null),
                "Builder should throw on null keywords set.");

        assertThrows(IllegalArgumentException.class,
                () -> builder.keywords(Set.of()),
                "Builder should throw on empty keywords set.");
    }

    @Test
    public void setsKeywordsCorrectly() {
        Set<String> keywords = new LinkedHashSet<>();
        keywords.add("trump");
        keywords.add("inauguration");
        builder.keywords(keywords);

        String expected = API_ENDPOINT + "q=" + String.join("+", keywords);
        String actual = builder.build().uri().toString();

        assertEquals(expected, actual,
                "Query builder should set plus-sign-separated keywords set in the request.");
    }

    @Test
    public void countryThrowsOnNullOrEmptyCountry() {
        assertThrows(IllegalArgumentException.class,
                () -> builder.country(null),
                "Builder should throw on null country name.");

        assertThrows(IllegalArgumentException.class,
                () -> builder.country(""),
                "Builder should throw on empty country name.");

        assertThrows(IllegalArgumentException.class,
                () -> builder.country("   "),
                "Builder should throw on blank country name.");
    }

    @Test
    public void setsCountryCorrectly() {
        builder.country("us");

        String expected = API_ENDPOINT + "country=us";
        String actual = builder.build().uri().toString();

        assertEquals(expected, actual,
                "Query builder should set country.");
    }

    @Test
    public void categoryThrowsOnNullCategory() {
        assertThrows(IllegalArgumentException.class,
                () -> builder.category(null),
                "Builder should throw on null category name.");
    }

    @Test
    public void setsCategoryCorrectly() {
        builder.category(NewsCategory.ENTERTAINMENT);

        String expected = API_ENDPOINT + "category=entertainment";
        String actual = builder.build().uri().toString();

        assertEquals(expected, actual,
                "Query builder should set category.");
    }

    @Test
    public void categoryThrowsOnInvalidPageSize() {
        assertThrows(IllegalArgumentException.class,
                () -> builder.paginate(0),
                "Builder should throw on page size that's less than or equal to 0.");

        assertThrows(IllegalArgumentException.class,
                () -> builder.paginate(-1),
                "Builder should throw on page size that's less than or equal to 0.");
    }

    @Test
    public void setsPageSizeCorrectly() {
        builder.paginate(345);

        String expected = API_ENDPOINT + "pageSize=345";
        String actual = builder.build().uri().toString();

        assertEquals(expected, actual,
                "Query builder should set page size.");
    }

    @Test
    public void categoryThrowsOnInvalidPageNumber() {
        assertThrows(IllegalArgumentException.class,
                () -> builder.page(0),
                "Builder should throw on page number that's less than or equal to 0.");

        assertThrows(IllegalArgumentException.class,
                () -> builder.page(-1),
                "Builder should throw on page number that's less than or equal to 0.");
    }

    @Test
    public void setsPageNumberCorrectly() {
        builder.page(325);

        String expected = API_ENDPOINT + "page=325";
        String actual = builder.build().uri().toString();

        assertEquals(expected, actual,
                "Query builder should set page number.");
    }

    @Test
    public void setsMultipleCriteriaCorrectly() {
        Set<String> sources = new LinkedHashSet<>();
        sources.add("cnn");
        sources.add("bbc");
        sources.add("der-spiegel");

        Set<String> keywords = new LinkedHashSet<>();
        keywords.add("trump");
        keywords.add("inauguration");

        builder.sources(sources)
                .keywords(keywords)
                .country("us")
                .category(NewsCategory.GENERAL)
                .paginate(345)
                .page(325);

        Set<String> expectedArguments = new HashSet<>(List.of("sources=cnn,bbc,der-spiegel",
                "q=trump+inauguration", "country=us", "category=general", "pageSize=345", "page=325"));

        String uri = builder.build().uri().toString();
        String argumentsListString = uri.split("[?]")[1];
        Set<String> actualArguments = new HashSet<>(Arrays.stream(argumentsListString
                        .split("&"))
                        .toList());

        assertEquals(expectedArguments.size(), actualArguments.size(),
                "The amount of arguments should be as much as the arguments changes called in the builder");

        var iterator = expectedArguments.iterator();
        while(iterator.hasNext()) {
            String currentArgument = iterator.next();
            assertTrue(actualArguments.contains(currentArgument),
                    "Arguments set should contain argument, but was not present");
        }
    }
}
