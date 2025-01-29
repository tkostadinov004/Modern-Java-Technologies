package bg.sofia.uni.fmi.mjt.splitwise.server.models;

public enum NotificationType {
    PERSONAL("Personal"),
    GROUP("Group");

    private String normalizedName;

    NotificationType(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    @Override
    public String toString() {
        return normalizedName;
    }
}
