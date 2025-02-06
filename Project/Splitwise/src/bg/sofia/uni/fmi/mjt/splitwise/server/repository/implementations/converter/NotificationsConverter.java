package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.NotificationDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class NotificationsConverter
        extends DataConverter<Map<User, Set<Notification>>, Notification, NotificationDTO> {
    private final UserRepository userRepository;

    public NotificationsConverter(CsvProcessor<NotificationDTO> csvProcessor, UserRepository userRepository) {
        super(csvProcessor);
        this.userRepository = userRepository;
    }

    @Override
    public Notification createFromDTO(NotificationDTO dto) {
        Optional<User> receiver = userRepository.getUserByUsername(dto.receiverUsername());
        if (receiver.isEmpty()) {
            return null;
        }
        return new Notification(receiver.get(), dto.content(), dto.timeSent(), dto.type());
    }
}

