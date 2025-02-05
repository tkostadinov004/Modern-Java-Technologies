package bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.PersonalDebtDTO;
import com.opencsv.CSVReader;

import java.util.Set;

public class PersonalDebtsCsvProcessor extends CsvProcessor<PersonalDebtDTO> {
    public PersonalDebtsCsvProcessor(CSVReader reader, String filePath) {
        super(reader, filePath);
    }

    private static final int DEBTOR_USERNAME_INDEX = 0;
    private static final int RECIPIENT_USERNAME_INDEX = 1;
    private static final int AMOUNT_INDEX = 2;
    private static final int REASON_INDEX = 3;

    private PersonalDebtDTO parsePersonalDebt(String[] args) {
        double amount;
        try {
            amount = Double.parseDouble(args[AMOUNT_INDEX]);
        } catch (NumberFormatException e) {
            return null;
        }

        return new PersonalDebtDTO(args[DEBTOR_USERNAME_INDEX],
                args[RECIPIENT_USERNAME_INDEX],
                amount,
                args[REASON_INDEX]);
    }

    @Override
    public Set<PersonalDebtDTO> readAll() {
        return super.readAll(this::parsePersonalDebt);
    }

    private String serializePersonalDebt(PersonalDebtDTO debt) {
        return "\"%s\",\"%s\",\"%s\",\"%s\""
                .formatted(debt.debtorUsername(),
                        debt.recipientUsername(),
                        debt.amount(), debt.reason());
    }

    @Override
    public synchronized void writeToFile(PersonalDebtDTO obj) {
        super.writeToFile(obj, this::serializePersonalDebt);
    }
}
