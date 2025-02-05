package bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.ExpenseDTO;
import com.opencsv.CSVReader;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ExpensesCsvProcessor extends CsvProcessor<ExpenseDTO> {
    public ExpensesCsvProcessor( CSVReader reader, String filePath) {
        super(reader, filePath);
    }

    private static final int NAME_INDEX = 0;
    private static final int AMOUNT_INDEX = 1;
    private static final int PURPOSE_INDEX = 2;
    private static final int TIMESTAMP_INDEX = 3;
    private static final int PARTICIPANTS_INDEX = 4;

    private ExpenseDTO parseExpense(String[] args) {
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

        Set<String> groupParticipantsUsernames = Arrays.stream(args[PARTICIPANTS_INDEX].split(","))
                .collect(Collectors.toSet());
        return new ExpenseDTO(args[NAME_INDEX], amount, args[PURPOSE_INDEX], date, groupParticipantsUsernames);
    }

    @Override
    public Set<ExpenseDTO> readAll() {
        return super.readAll(this::parseExpense);
    }

    private String serializeExpense(ExpenseDTO expense) {
        String participants = String.join(",", new HashSet<>(expense
                .participantsUsernames()));

        return "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\""
                .formatted(expense.payerUsername(),
                        expense.amount(),
                        expense.reason(),
                        DATETIME_PARSE_FORMAT.format(expense.timestamp()),
                        participants);
    }

    @Override
    public synchronized void writeToFile(ExpenseDTO obj) {
        super.writeToFile(obj, this::serializeExpense);
    }
}
