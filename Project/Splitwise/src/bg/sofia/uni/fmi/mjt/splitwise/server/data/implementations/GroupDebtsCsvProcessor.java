package bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.GroupDebtDTO;
import com.opencsv.CSVReader;

import java.util.Set;

public class GroupDebtsCsvProcessor extends CsvProcessor<GroupDebtDTO> {
    public GroupDebtsCsvProcessor(CSVReader reader, String filePath) {
        super(reader, filePath);
    }

    private static final int DEBTOR_USERNAME_INDEX = 0;
    private static final int RECIPIENT_USERNAME_INDEX = 1;
    private static final int GROUP_INDEX = 2;
    private static final int AMOUNT_INDEX = 3;
    private static final int REASON_INDEX = 4;

    private GroupDebtDTO parseGroupDebt(String[] args) {
        double amount;
        try {
            amount = Double.parseDouble(args[AMOUNT_INDEX]);
        } catch (NumberFormatException e) {
            return null;
        }

        return new GroupDebtDTO(args[DEBTOR_USERNAME_INDEX],
                args[RECIPIENT_USERNAME_INDEX],
                args[GROUP_INDEX],
                amount,
                args[REASON_INDEX]);
    }

    @Override
    public Set<GroupDebtDTO> readAll() {
        return super.readAll(this::parseGroupDebt);
    }

    private String serializePersonalDebt(GroupDebtDTO debt) {
        return "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\""
                .formatted(debt.debtorUsername(),
                        debt.recipientUsername(),
                        debt.groupName(),
                        debt.amount(),
                        debt.reason());
    }

    @Override
    public synchronized void writeToFile(GroupDebtDTO obj) {
        super.writeToFile(obj, this::serializePersonalDebt);
    }
}
