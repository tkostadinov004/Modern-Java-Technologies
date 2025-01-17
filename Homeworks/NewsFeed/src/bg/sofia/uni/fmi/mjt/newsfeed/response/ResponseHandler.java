package bg.sofia.uni.fmi.mjt.newsfeed.response;

import bg.sofia.uni.fmi.mjt.newsfeed.response.pagination.NewsResponsePage;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.Error;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.StatusCode;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.LimitedRateException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.NewsFeedResponseException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.ParameterException;
import bg.sofia.uni.fmi.mjt.newsfeed.response.status.exception.SourcesException;
import com.google.gson.Gson;

public class ResponseHandler {
    private int statusCode;
    private String body;

    public ResponseHandler(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public NewsResponsePage deserializePage() throws
            LimitedRateException, ParameterException, SourcesException, NewsFeedResponseException {
        Gson gson = new Gson();
        if (statusCode == StatusCode.OK.getValue()) {
            return gson.fromJson(body, NewsResponsePage.class);
        }

        Error status = gson.fromJson(body, Error.class);
        switch (status.code()) {
            case "parameterInvalid", "parametersMissing" -> throw new ParameterException(status.message());
            case "rateLimited" -> throw new LimitedRateException(status.message());
            case "sourcesTooMany", "sourceDoesNotExist"  -> throw new SourcesException(status.message());
            default -> throw new NewsFeedResponseException(status.message());
        }
    }
}
