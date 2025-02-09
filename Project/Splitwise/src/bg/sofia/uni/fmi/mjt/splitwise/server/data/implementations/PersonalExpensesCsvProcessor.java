package bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.PersonalExpenseDTO;
import com.opencsv.CSVReader;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Set;

public class PersonalExpensesCsvProcessor extends CsvProcessor<PersonalExpenseDTO> {
    public PersonalExpensesCsvProcessor(CSVReader reader, String filePath) {
        super(reader, filePath);
    }

    private static final int PAYER_NAME_INDEX = 0;
    private static final int DEBTOR_NAME_INDEX = 1;
    private static final int AMOUNT_INDEX = 2;
    private static final int PURPOSE_INDEX = 3;
    private static final int TIMESTAMP_INDEX = 4;

    private PersonalExpenseDTO parseExpense(String[] args) {
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

        return new PersonalExpenseDTO(args[PAYER_NAME_INDEX], args[DEBTOR_NAME_INDEX], amount, args[PURPOSE_INDEX],
                date);
    }

    @Override
    public Set<PersonalExpenseDTO> readAll() {
        return super.readAll(this::parseExpense);
    }

    private String serializeExpense(PersonalExpenseDTO expense) {
        return "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\""
                .formatted(expense.payerUsername(),
                        expense.debtorUsername(),
                        expense.amount(),
                        expense.reason(),
                        DATETIME_PARSE_FORMAT.format(expense.timestamp()));
    }

    @Override
    public synchronized void writeToFile(PersonalExpenseDTO obj) {
        super.writeToFile(obj, this::serializeExpense);
    }
}
