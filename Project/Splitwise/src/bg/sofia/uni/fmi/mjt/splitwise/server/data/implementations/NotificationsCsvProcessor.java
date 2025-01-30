package bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import com.opencsv.CSVReader;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Set;

public class NotificationsCsvProcessor extends CsvProcessor<Notification> {
    private final UserRepository userRepository;

    public NotificationsCsvProcessor(UserRepository userRepository, CSVReader reader, String filePath) {
        super(reader, filePath);
        this.userRepository = userRepository;
    }

    private static final int RECEIVER_USERNAME_INDEX = 0;
    private static final int CONTENT_INDEX = 1;
    private static final int DATE_INDEX = 2;
    private static final int TYPE_INDEX = 3;

    private Notification parseNotification(String[] args) {
        Optional<User> receiver = userRepository.getUserByUsername(args[RECEIVER_USERNAME_INDEX]);
        if (receiver.isEmpty()) {
            return null;
        }

        LocalDateTime date;
        try {
            date = LocalDateTime.parse(args[DATE_INDEX], DATETIME_PARSE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }

        NotificationType type;
        try {
            type = Enum.valueOf(NotificationType.class, args[TYPE_INDEX]);
        } catch (IllegalArgumentException e) {
            return null;
        }

        return new Notification(receiver.get(), args[CONTENT_INDEX], date, type);
    }

    @Override
    public Set<Notification> readAll() {
        return super.readAll(this::parseNotification);
    }

    private String serializeNotification(Notification notification) {
        return "\"%s\",\"%s\",\"%s\",\"%s\""
                .formatted(notification.receiver().username(), notification.content(),
                        DATETIME_PARSE_FORMAT.format(notification.timeSent()),
                        notification.type().name());
    }

    @Override
    public synchronized void writeToFile(Notification obj) {
        super.writeToFile(obj, this::serializeNotification);
    }
}
