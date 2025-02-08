package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.NotificationsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultNotificationsRepositoryTest {
    private static final DependencyContainer dependencyContainer = mock();
    private static final User USER_1 = new User("user1", "asd", "Test", "Test1");
    private static final User USER_2 = new User("user2", "asd", "Test", "Test1");
    private static final User USER_3 = new User("user3", "asd", "Test", "Test1");

    @BeforeAll
    public static void setUp() {
        NotificationsCsvProcessor csvProcessor = mock();
        when(csvProcessor.readAll())
                .thenReturn(Set.of());
        doAnswer((Answer<Void>) _ -> null)
                .when(csvProcessor).writeToFile(any());
        when(dependencyContainer.get(NotificationsCsvProcessor.class))
                .thenReturn(csvProcessor);

        UserRepository userRepository = mock();
        when(userRepository.getUserByUsername("user1")).thenReturn(Optional.of(USER_1));
        when(userRepository.getUserByUsername("user2")).thenReturn(Optional.of(USER_2));
        when(userRepository.getUserByUsername("user3")).thenReturn(Optional.of(USER_3));
        when(userRepository.containsUser("user1")).thenReturn(true);
        when(userRepository.containsUser("user2")).thenReturn(true);
        when(userRepository.containsUser("user3")).thenReturn(true);
        when(dependencyContainer.get(UserRepository.class))
                .thenReturn(userRepository);
    }

    @Test
    public void testGetNotificationsForUserThrowsOnInvalidUsername() {
        NotificationsRepository notificationsRepository = new DefaultNotificationsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class, () -> notificationsRepository.getNotificationsForUser(null),
                "getNotificationsForUser() should throw on null username");
        assertThrows(IllegalArgumentException.class, () -> notificationsRepository.getNotificationsForUser(""),
                "getNotificationsForUser() should throw on empty username");
        assertThrows(IllegalArgumentException.class, () -> notificationsRepository.getNotificationsForUser("   "),
                "getNotificationsForUser() should throw on blank username");
    }

    @Test
    public void testGetNotificationsForUserThrowsOnNonexistentUser() {
        NotificationsRepository notificationsRepository = new DefaultNotificationsRepository(dependencyContainer);

        assertThrows(NonExistentUserException.class, () -> notificationsRepository.getNotificationsForUser( "asdasdasd"),
                "getNotificationsForUser() should throw on non existing user");
    }

    @Test
    public void testGetFriendsReturnsEmptySetIfUserHasNoFriends() {
        NotificationsRepository notificationsRepository = new DefaultNotificationsRepository(dependencyContainer);
        assertTrue(notificationsRepository.getNotificationsForUser("user3").isEmpty(),
                "getNotificationsForUser() should return an empty set if a user has no notifications");
    }

    @Test
    public void testGetFriendsReturnsCorrectly() {
        NotificationsRepository notificationsRepository = new DefaultNotificationsRepository(dependencyContainer);
        Notification notification1 = new Notification(USER_1, "test content1", LocalDateTime.now(), NotificationType.GROUP);
        Notification notification2 = new Notification(USER_1, "test content2", LocalDateTime.now(), NotificationType.PERSONAL);
        notificationsRepository.addNotificationForUser(notification1.receiver().username(), notification1.content(), notification1.timeSent(), notification1.type());
        notificationsRepository.addNotificationForUser(notification2.receiver().username(), notification2.content(), notification2.timeSent(), notification2.type());

        Set<Notification> expected = Set.of(notification1, notification2);
        Set<Notification> actual = notificationsRepository.getNotificationsForUser("user1");

        assertTrue(expected.size() == actual.size() && expected.containsAll(actual),
                "All notifications of a user should be present in the repository.");
    }

    @Test
    public void testAddNotificationForUserThrowsOnInvalidReceiverUsername() {
        NotificationsRepository notificationsRepository = new DefaultNotificationsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> notificationsRepository.addNotificationForUser( null , "asd", LocalDateTime.now(), NotificationType.PERSONAL),
                "addNotificationForUser() should throw on null receiver username");
        assertThrows(IllegalArgumentException.class,
                () -> notificationsRepository.addNotificationForUser( "" ,"asd",LocalDateTime.now(), NotificationType.PERSONAL),
                "addNotificationForUser() should throw on empty receiver username");
        assertThrows(IllegalArgumentException.class,
                () -> notificationsRepository.addNotificationForUser("   ", "asd", LocalDateTime.now(), NotificationType.PERSONAL),
                "addNotificationForUser() should throw on blank receiver username");
    }

    @Test
    public void testAddNotificationForUserThrowsOnInvalidContent() {
        NotificationsRepository notificationsRepository = new DefaultNotificationsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> notificationsRepository.addNotificationForUser(  "user1",null , LocalDateTime.now(), NotificationType.PERSONAL),
                "addNotificationForUser() should throw on null content");
        assertThrows(IllegalArgumentException.class,
                () -> notificationsRepository.addNotificationForUser( "user1","" ,LocalDateTime.now(), NotificationType.PERSONAL),
                "addNotificationForUser() should throw on empty content");
        assertThrows(IllegalArgumentException.class,
                () -> notificationsRepository.addNotificationForUser( "user1","   ", LocalDateTime.now(), NotificationType.PERSONAL),
                "addNotificationForUser() should throw on blank content");
    }

    @Test
    public void testAddNotificationForUserThrowsOnNullDate() {
        NotificationsRepository notificationsRepository = new DefaultNotificationsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> notificationsRepository.addNotificationForUser(  "user1", "content", null, NotificationType.PERSONAL),
                "addNotificationForUser() should throw on null date");
    }

    @Test
    public void testAddNotificationForUserThrowsOnNullType() {
        NotificationsRepository notificationsRepository = new DefaultNotificationsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class,
                () -> notificationsRepository.addNotificationForUser(  "user1", "content", LocalDateTime.now(), null),
                "addNotificationForUser() should throw on null notification type");
    }

    @Test
    public void testAddNotificationForUserThrowsOnNonexistentReceiver() {
        NotificationsRepository notificationsRepository = new DefaultNotificationsRepository(dependencyContainer);

        assertThrows(NonExistentUserException.class, () -> notificationsRepository.addNotificationForUser( "asdasdasd", "content", LocalDateTime.now(), NotificationType.PERSONAL),
                "addNotificationForUser() should throw on non existing receiver");
    }

    @Test
    public void testAddNotificationForUserAddsCorrectly() {
        NotificationsRepository notificationsRepository = new DefaultNotificationsRepository(dependencyContainer);
        Notification notification = new Notification(USER_1, "content", LocalDateTime.now(), NotificationType.GROUP);
        notificationsRepository.addNotificationForUser(notification.receiver().username(), notification.content(), notification.timeSent(), notification.type());

        assertTrue(notificationsRepository
                .getNotificationsForUser(notification.receiver().username())
                .contains(notification),
                "Notification should be added in the repository");
    }

    @Test
    public void testAddNotificationForUserAddsCorrectlyWithoutGivenTime() {
        NotificationsRepository notificationsRepository = new DefaultNotificationsRepository(dependencyContainer);
        Notification notification = new Notification(USER_1, "content", LocalDateTime.now(), NotificationType.GROUP);
        notificationsRepository.addNotificationForUser(notification.receiver().username(), notification.content(), notification.type());

        Optional<Notification> found = notificationsRepository
                .getNotificationsForUser(notification.receiver().username())
                .stream().filter(n -> n.content().equals(notification.content()) && n.type().equals(notification.type()))
                .findFirst();

        assertTrue(found.isPresent(),
                "Notification should be added in the repository");
        assertTrue(notification.receiver().equals(found.get().receiver())
                && notification.type().equals(found.get().type())
                && notification.content().equals(found.get().content()),
                "Notification should be added in the repository");
    }

    @Test
    public void testRemoveAllNotificationsForUserThrowsOnInvalidUsername() {
        NotificationsRepository notificationsRepository = new DefaultNotificationsRepository(dependencyContainer);

        assertThrows(IllegalArgumentException.class, () -> notificationsRepository.removeAllNotificationsForUser(null),
                "removeAllNotificationsForUser() should throw on null username");
        assertThrows(IllegalArgumentException.class, () -> notificationsRepository.removeAllNotificationsForUser(""),
                "removeAllNotificationsForUser() should throw on empty username");
        assertThrows(IllegalArgumentException.class, () -> notificationsRepository.removeAllNotificationsForUser("   "),
                "removeAllNotificationsForUser() should throw on blank username");
    }

    @Test
    public void testRemoveAllNotificationsForUserThrowsOnNonexistentUser() {
        NotificationsRepository notificationsRepository = new DefaultNotificationsRepository(dependencyContainer);

        assertThrows(NonExistentUserException.class, () -> notificationsRepository.removeAllNotificationsForUser( "asdasdasd"),
                "removeAllNotificationsForUser() should throw on non existing user");
    }

    @Test
    public void testRemoveAllNotificationsForUserCorrectly() {
        NotificationsRepository notificationsRepository = new DefaultNotificationsRepository(dependencyContainer);
        Notification notification1 = new Notification(USER_1, "content", LocalDateTime.now(), NotificationType.GROUP);
        Notification notification2 = new Notification(USER_1, "content1", LocalDateTime.now(), NotificationType.PERSONAL);
        notificationsRepository.addNotificationForUser(notification1.receiver().username(), notification1.content(), notification1.timeSent(), notification1.type());
        notificationsRepository.addNotificationForUser(notification2.receiver().username(), notification2.content(), notification2.timeSent(), notification2.type());

        notificationsRepository.removeAllNotificationsForUser(notification1.receiver().username());

        assertTrue(notificationsRepository.getNotificationsForUser(notification1.receiver().username()).isEmpty(),
                "All notifications of a given user should be deleted from the repository");
    }
}
