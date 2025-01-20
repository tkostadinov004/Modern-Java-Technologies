package bg.sofia.uni.fmi.mjt.newsfeed.response;

import bg.sofia.uni.fmi.mjt.newsfeed.response.pagination.NewsResponsePage;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.Error;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.StatusCode;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LimitedRateException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LogicalParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.NewsFeedResponseException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.MissingParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.SourcesException;
import com.google.gson.Gson;

import java.net.http.HttpRequest;

public class ResponseHandler {
    private int statusCode;
    private HttpRequest request;
    private String body;

    public ResponseHandler(int statusCode, HttpRequest request, String body) {
        this.statusCode = statusCode;
        this.request = request;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public String getBody() {
        return body;
    }

    public NewsResponsePage deserializePage() throws
            LimitedRateException, MissingParameterException,
            LogicalParameterException, SourcesException, NewsFeedResponseException {
        Gson gson = new Gson();
        if (statusCode == StatusCode.OK.getValue()) {
            return gson.fromJson(body, NewsResponsePage.class);
        }

        Error status = gson.fromJson(body, Error.class);
        switch (status.code()) {
            case "parameterInvalid" -> throw new LogicalParameterException(status.message());
            case "parametersMissing" ->
                    throw new MissingParameterException("At least one of the following parameters should be " +
                            "included in the search query: country, language, keywords, category");
            case "rateLimited" -> throw new LimitedRateException(status.message());
            case "sourcesTooMany", "sourceDoesNotExist"  -> throw new SourcesException(status.message());
            default -> throw new NewsFeedResponseException(status.message());
        }
    }
}
