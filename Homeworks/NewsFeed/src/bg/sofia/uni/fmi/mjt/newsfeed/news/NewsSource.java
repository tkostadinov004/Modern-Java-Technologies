package bg.sofia.uni.fmi.mjt.newsfeed.news;

public record NewsSource(String id, String name) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Id - ").append(id).append(System.lineSeparator());
        sb.append("Name - ").append(name);

        return sb.toString();
    }
}
