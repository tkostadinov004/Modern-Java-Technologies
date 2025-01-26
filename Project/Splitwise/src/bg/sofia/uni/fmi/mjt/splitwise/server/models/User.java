package bg.sofia.uni.fmi.mjt.splitwise.server.models;

public record User(String username, String hashedPass, String firstName, String lastName) {
    @Override
    public String toString() {
        return "%s %s (%s)".formatted(firstName, lastName, username);
    }
}
