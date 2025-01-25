package bg.sofia.uni.fmi.mjt.splitwise.server.command.factory;

public interface Factory<T> {
    T build(String input);
}
