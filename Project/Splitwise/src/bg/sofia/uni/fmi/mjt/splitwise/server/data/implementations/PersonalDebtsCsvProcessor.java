package bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.PersonalDebtDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import com.opencsv.CSVReader;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class PersonalDebtsCsvProcessor extends CsvProcessor<PersonalDebtDTO> {
    private final UserRepository userRepository;

    public PersonalDebtsCsvProcessor(UserRepository userRepository, CSVReader reader, String filePath) {
        super(reader, filePath);
        this.userRepository = userRepository;
    }

    private static final int DEBTOR_USERNAME_INDEX = 0;
    private static final int RECIPIENT_USERNAME_INDEX = 1;
    private static final int AMOUNT_INDEX = 2;
    private static final int REASON_INDEX = 3;

    private PersonalDebtDTO parsePersonalDebt(String[] args) {
        Optional<User> debtor = userRepository.getUserByUsername(args[DEBTOR_USERNAME_INDEX]);
        if (debtor.isEmpty()) {
            return null;
        }

        Optional<User> recipient = userRepository.getUserByUsername(args[RECIPIENT_USERNAME_INDEX]);
        if (recipient.isEmpty()) {
            return null;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[AMOUNT_INDEX]);
        } catch (NumberFormatException e) {
            return null;
        }

        return new PersonalDebtDTO(debtor.get(), recipient.get(), amount, args[REASON_INDEX]);
    }

    @Override
    public Set<PersonalDebtDTO> readAll() {
        return super.readAll(this::parsePersonalDebt);
    }

    private String serializePersonalDebt(PersonalDebtDTO debt) {
        return "\"%s\",\"%s\",\"%s\",\"%s\""
                .formatted(debt.debtor().username(),
                        debt.recipient().username(),
                        debt.amount(), debt.reason());
    }

    @Override
    public synchronized void writeToFile(PersonalDebtDTO obj) {
        super.writeToFile(obj, this::serializePersonalDebt);
    }
}
