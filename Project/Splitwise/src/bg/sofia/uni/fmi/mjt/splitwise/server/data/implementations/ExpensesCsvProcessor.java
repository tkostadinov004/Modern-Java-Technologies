package bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Expense;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import com.opencsv.CSVReader;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ExpensesCsvProcessor extends CsvProcessor<Expense> {
    private final UserRepository userRepository;

    public ExpensesCsvProcessor(UserRepository userRepository, CSVReader reader, String filePath) {
        super(reader, filePath);
        this.userRepository = userRepository;
    }

    private static final int NAME_INDEX = 0;
    private static final int AMOUNT_INDEX = 1;
    private static final int PURPOSE_INDEX = 2;
    private static final int TIMESTAMP_INDEX = 3;
    private static final int PARTICIPANTS_INDEX = 4;

    private User parseUser(String username) {
        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return user.get();
    }

    private Set<User> parseGroupParticipants(String arg) {
        try {
            return Arrays.stream(arg.split(","))
                    .map(this::parseUser)
                    .collect(Collectors.toSet());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Expense parseExpense(String[] args) {
        Optional<User> payer = userRepository.getUserByUsername(args[NAME_INDEX]);
        if (payer.isEmpty()) {
            return null;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[AMOUNT_INDEX]);
        } catch (NumberFormatException e) {
            return null;
        }

        LocalDateTime date;
        try {
            date = LocalDateTime.parse(args[TIMESTAMP_INDEX], DATETIME_PARSE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }

        Set<User> groupParticipants = parseGroupParticipants(args[PARTICIPANTS_INDEX]);
        if (groupParticipants == null) {
            return null;
        }
        return new Expense(payer.get(), amount, args[PURPOSE_INDEX], date, groupParticipants);
    }

    @Override
    public Set<Expense> readAll() {
        return super.readAll(this::parseExpense);
    }

    private String serializeExpense(Expense expense) {
        String participants = String.join(",", expense
                .participants()
                .stream()
                .map(user -> user.username())
                .collect(Collectors.toSet()));

        return "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\""
                .formatted(expense.payer().username(), expense.amount(), expense.purpose(),
                        DATETIME_PARSE_FORMAT.format(expense.timestamp()), participants);
    }

    @Override
    public synchronized void writeToFile(Expense obj) {
        super.writeToFile(obj, this::serializeExpense);
    }
}
