package bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.NotificationDTO;
import com.opencsv.CSVReader;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Set;

public class NotificationsCsvProcessor extends CsvProcessor<NotificationDTO> {
    public NotificationsCsvProcessor(CSVReader reader, String filePath) {
        super(reader, filePath);
    }

    private static final int RECEIVER_USERNAME_INDEX = 0;
    private static final int CONTENT_INDEX = 1;
    private static final int DATE_INDEX = 2;
    private static final int TYPE_INDEX = 3;

    private NotificationDTO parseNotification(String[] args) {
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

        return new NotificationDTO(args[RECEIVER_USERNAME_INDEX], args[CONTENT_INDEX], date, type);
    }

    @Override
    public Set<NotificationDTO> readAll() {
        return super.readAll(this::parseNotification);
    }

    private String serializeNotification(NotificationDTO notification) {
        return "\"%s\",\"%s\",\"%s\",\"%s\""
                .formatted(notification.receiverUsername(), notification.content(),
                        DATETIME_PARSE_FORMAT.format(notification.timeSent()),
                        notification.type().name());
    }

    @Override
    public synchronized void writeToFile(NotificationDTO obj) {
        super.writeToFile(obj, this::serializeNotification);
    }
}
