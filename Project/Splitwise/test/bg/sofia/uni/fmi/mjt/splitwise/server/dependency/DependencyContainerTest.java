package bg.sofia.uni.fmi.mjt.splitwise.server.dependency;

import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.exception.DependencyNotFoundException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.DefaultUserRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class DependencyContainerTest {
    @Test
    public void testRegisterAddsServiceToMap() {
        UserRepository repository = mock();
        DependencyContainer container = new DependencyContainer();
        container.register(UserRepository.class, repository);

        assertDoesNotThrow(() -> container.get(UserRepository.class),
                "Service should be in the container after it is registered");
        assertEquals(repository, container.get(UserRepository.class),
                "Service should be in the container after it is registered");
    }

    @Test
    public void testGetThrowsIfServiceIsNotRegistered() {
        DependencyContainer container = new DependencyContainer();

        assertThrows(DependencyNotFoundException.class, () -> container.get(UserRepository.class),
                "An exception should be thrown if a service is not registered in the container");
    }
}
