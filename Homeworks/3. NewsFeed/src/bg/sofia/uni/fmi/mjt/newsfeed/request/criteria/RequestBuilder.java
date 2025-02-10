package bg.sofia.uni.fmi.mjt.newsfeed.request.criteria;

import java.util.Set;

public interface RequestBuilder {
    /**
     * Sets a sources criteria for the query, meaning that every article will be sourced
     * from the given sources only.
     *
     * @param sources a set of sources to filter by
     * @return the current instance of RequestBuilder
     * @throws IllegalArgumentException if sources is null or empty
     */
    RequestBuilder sources(Set<String> sources);

    /**
     * Sets a keywords criteria for the query, meaning that only articles, containing the
     * given keywords will be fetched.
     *
     * @param keywords a set of keywords to filter by
     * @return the current instance of RequestBuilder
     * @throws IllegalArgumentException if keywords is null or empty
     */
    RequestBuilder keywords(Set<String> keywords);

    /**
     * Sets a country criteria for the query, meaning that only articles, originating from a
     * given country will be fetched.
     *
     * @param country the country of origin of a news article
     * @return the current instance of RequestBuilder
     * @throws IllegalArgumentException if country is null, empty or blank
     */
    RequestBuilder country(String country);

    /**
     * Sets a category criteria for the query, meaning that only articles, belonging to
     * this category will be fetched
     *
     * @param category the category of a news article
     * @return the current instance of RequestBuilder
     * @throws IllegalArgumentException if category is null
     */
    RequestBuilder category(NewsCategory category);

    /**
     * Separates the query result into pages, each with pageSize size.
     *
     * @param pageSize the amount of articles in a page
     * @return the current instance of RequestBuilder
     * @throws IllegalArgumentException if pageSize is less than or equal to 0
     */
    RequestBuilder paginate(int pageSize);

    /**
     * Sets the current result page to be the given one.
     *
     * @param page the amount of articles in a page
     * @return the current instance of RequestBuilder
     * @throws IllegalArgumentException if page is less than or equal to 0
     */
    RequestBuilder page(int page);

    /**
     * Builds the request from the current instance of the builder
     *
     * @return a request built from the current instance of the builder
     */
    FetchRequest build();
}
