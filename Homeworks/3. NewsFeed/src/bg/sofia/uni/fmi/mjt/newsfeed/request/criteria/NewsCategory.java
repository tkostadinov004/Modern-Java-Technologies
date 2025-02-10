package bg.sofia.uni.fmi.mjt.newsfeed.request.criteria;

public enum NewsCategory {
    BUSINESS,
    ENTERTAINMENT,
    GENERAL,
    HEALTH,
    SCIENCE,
    SPORTS,
    TECHNOLOGY;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
