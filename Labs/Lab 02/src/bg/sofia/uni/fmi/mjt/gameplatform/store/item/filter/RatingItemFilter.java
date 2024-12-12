package bg.sofia.uni.fmi.mjt.gameplatform.store.item.filter;

import bg.sofia.uni.fmi.mjt.gameplatform.store.item.StoreItem;

import java.math.BigDecimal;

public class RatingItemFilter implements ItemFilter {
    private double rating;

    public RatingItemFilter(double rating) {
        setRating(rating);
    }

    @Override
    public boolean matches(StoreItem item) {
        return item.getRating() >= rating;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}