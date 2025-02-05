package bg.sofia.uni.fmi.mjt.splitwise.server.dependency;

import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.exception.DependencyNotFoundException;

import java.util.HashMap;
import java.util.Map;

public class DependencyContainer {
    private final Map<Class<?>, Object> dependencies;

    public DependencyContainer() {
        dependencies = new HashMap<>();
    }

    public <T> T get(Class<T> type) {
        if (!dependencies.containsKey(type)) {
            throw new DependencyNotFoundException("Dependency of type %s not found!"
                    .formatted(type.getName()));
        }
        return (T) dependencies.get(type);
    }

    public <T> DependencyContainer register(Class<T> type, T instance) {
        dependencies.put(type, instance);
        return this;
    }
}
