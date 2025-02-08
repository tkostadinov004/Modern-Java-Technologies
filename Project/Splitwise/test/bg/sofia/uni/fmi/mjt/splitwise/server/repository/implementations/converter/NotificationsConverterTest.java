package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.PersonalExpense;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.NotificationDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.PersonalExpenseDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NotificationsConverterTest {
    private static final CsvProcessor<NotificationDTO> CSV_PROCESSOR = mock();
    private static final UserRepository USER_REPOSITORY = mock();
    private static final User USER_1 = new User("USER_1", "asd", "Test", "Test1");
    private static final User USER_2 = new User("USER_2", "asd", "Test", "Test1");
    private static final User USER_3 = new User("USER_3", "asd", "Test", "Test1");
    private static final Collector<Notification, ?, Map<User, Set<Notification>>> COLLECTOR = Collectors.groupingBy(Notification::receiver,
            Collectors.mapping(notification -> notification, Collectors.toSet()));
    private static final NotificationDTO DTO_1 = new NotificationDTO("USER_1", "test reason1", LocalDateTime.now(), NotificationType.PERSONAL);
    private static final NotificationDTO DTO_2 = new NotificationDTO("USER_2",  "test reason2", LocalDateTime.now(), NotificationType.GROUP);
    private static final NotificationDTO DTO_3 = new NotificationDTO("USER_1",  "test reason3", LocalDateTime.now(), NotificationType.PERSONAL);
    private static final NotificationDTO DTO_4 = new NotificationDTO("USER_3",  "test reason4", LocalDateTime.now(), NotificationType.GROUP);
    private static final NotificationDTO DTO_5 = new NotificationDTO("user4", "test reason6", LocalDateTime.now(), NotificationType.PERSONAL);


    @BeforeAll
    public static void setUp() {
        when(CSV_PROCESSOR.readAll())
                .thenReturn(Set.of(DTO_1, DTO_2, DTO_3, DTO_4, DTO_5));
        when(USER_REPOSITORY.getUserByUsername("USER_1")).thenReturn(Optional.of(USER_1));
        when(USER_REPOSITORY.getUserByUsername("USER_2")).thenReturn(Optional.of(USER_2));
        when(USER_REPOSITORY.getUserByUsername("USER_3")).thenReturn(Optional.of(USER_3));
    }

    @Test
    public void testPopulateIgnoresEntriesWithNonExistingUsers() {
        NotificationsConverter converter = new NotificationsConverter(CSV_PROCESSOR, USER_REPOSITORY);

        var result = converter.populate(COLLECTOR);

        assertFalse(result.keySet().stream().anyMatch(u -> u.username().equals("user4")),
                "Notifications for users that are not present in the repository should be ignored");
    }

    @Test
    public void testPopulatePopulatesSuccessfully() {
        Notification notification1 = new Notification(USER_1,"test reason1", DTO_1.timeSent(), NotificationType.PERSONAL);
        Notification notification2 = new Notification(USER_2,"test reason2", DTO_2.timeSent(), NotificationType.GROUP);
        Notification notification3 = new Notification(USER_1, "test reason3", DTO_3.timeSent(), NotificationType.PERSONAL);
        Notification notification4 = new Notification(USER_3,"test reason4", DTO_4.timeSent(), NotificationType.GROUP);

        NotificationsConverter converter = new NotificationsConverter(CSV_PROCESSOR, USER_REPOSITORY);
        var expected = new HashMap<User, Set<Notification>>();
        expected.put(USER_1, Set.of(notification1, notification3));
        expected.put(USER_2, Set.of(notification2));
        expected.put(USER_3, Set.of(notification4));

        var actual = converter.populate(COLLECTOR);

        assertEquals(expected, actual,
                "Only entries with users present in the repository should be left and mapped properly");
    }
}

