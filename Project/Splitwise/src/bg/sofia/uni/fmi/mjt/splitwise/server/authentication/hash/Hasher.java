package bg.sofia.uni.fmi.mjt.splitwise.server.authentication.hash;

public interface Hasher {
    String hash(String content);
}
