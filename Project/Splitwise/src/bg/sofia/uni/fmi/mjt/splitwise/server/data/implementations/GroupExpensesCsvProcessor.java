package bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.GroupExpenseDTO;
import com.opencsv.CSVReader;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Set;

public class GroupExpensesCsvProcessor  extends CsvProcessor<GroupExpenseDTO> {
    public GroupExpensesCsvProcessor(CSVReader reader, String filePath) {
        super(reader, filePath);
    }

    private static final int NAME_INDEX = 0;
    private static final int AMOUNT_INDEX = 1;
    private static final int PURPOSE_INDEX = 2;
    private static final int GROUP_INDEX = 3;
    private static final int TIMESTAMP_INDEX = 4;

    private GroupExpenseDTO parseExpense(String[] args) {
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

        return new GroupExpenseDTO(args[NAME_INDEX], amount, args[PURPOSE_INDEX], args[GROUP_INDEX], date);
    }

    @Override
    public Set<GroupExpenseDTO> readAll() {
        return super.readAll(this::parseExpense);
    }

    private String serializeExpense(GroupExpenseDTO expense) {
        return "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\""
                .formatted(expense.payerUsername(),
                        expense.amount(),
                        expense.reason(),
                        expense.groupName(),
                        DATETIME_PARSE_FORMAT.format(expense.timestamp()));
    }

    @Override
    public synchronized void writeToFile(GroupExpenseDTO obj) {
        super.writeToFile(obj, this::serializeExpense);
    }
}
