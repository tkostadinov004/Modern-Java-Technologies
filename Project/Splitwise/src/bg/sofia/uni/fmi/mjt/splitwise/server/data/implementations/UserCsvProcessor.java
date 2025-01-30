package bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import com.opencsv.CSVReader;

import java.nio.file.Path;
import java.util.Set;

public class UserCsvProcessor extends CsvProcessor<User> {
    public UserCsvProcessor(CSVReader reader, String filePath) {
        super(reader, filePath);
    }

    private static final int USERNAME_INDEX = 0;
    private static final int PASSWORD_INDEX = 1;
    private static final int FIRST_NAME_INDEX = 2;
    private static final int LAST_NAME_INDEX = 3;

    private User parseUser(String[] args) {
        return new User(args[USERNAME_INDEX],
                args[PASSWORD_INDEX],
                args[FIRST_NAME_INDEX],
                args[LAST_NAME_INDEX]);
    }

    @Override
    public Set<User> readAll() {
        return super.readAll(this::parseUser);
    }

    private String serializeUser(User user) {
        return "\"%s\",\"%s\",\"%s\",\"%s\""
                .formatted(user.username(), user.hashedPass(), user.firstName(), user.lastName());
    }

    @Override
    public synchronized void writeToFile(User obj) {
        super.writeToFile(obj, this::serializeUser);
    }
}
