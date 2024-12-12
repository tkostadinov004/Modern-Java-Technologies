package bg.sofia.uni.fmi.mjt.gameplatform.store.item.category;

import bg.sofia.uni.fmi.mjt.gameplatform.store.item.StoreItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public abstract class BaseCategory implements StoreItem {
    protected String title;
    protected BigDecimal price;
    protected LocalDateTime releaseDate;
    protected double rating;
    protected int ratingsCount;

    protected BaseCategory(String title, BigDecimal price, LocalDateTime releaseDate) {
        setTitle(title);
        setPrice(price);
        setReleaseDate(releaseDate);
    }
    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public BigDecimal getPrice() {
        return this.price;
    }

    @Override
    public double getRating() {
        return this.rating / ratingsCount;
    }

    @Override
    public LocalDateTime getReleaseDate() {
        return this.releaseDate;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void setPrice(BigDecimal price) {
        this.price = price;
        this.price = this.price.setScale(2, RoundingMode.FLOOR);
    }

    @Override
    public void setReleaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
    }

    @Override
    public void rate(double rating) {
        this.rating += rating;
        ratingsCount++;
    }
}
