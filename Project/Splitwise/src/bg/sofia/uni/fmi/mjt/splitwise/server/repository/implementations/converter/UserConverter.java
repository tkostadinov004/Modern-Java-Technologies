package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;

import java.util.Set;

public class UserConverter extends DataConverter<Set<User>, User, User> {
    public UserConverter(CsvProcessor<User> userCsvProcessor) {
        super(userCsvProcessor);
    }

    @Override
    public User createFromDTO(User user) {
        return user;
    }
}
