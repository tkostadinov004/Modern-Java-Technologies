package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.PersonalExpense;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.PersonalExpenseDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PersonalExpensesConverter
        extends DataConverter<Map<User, Set<PersonalExpense>>, PersonalExpense, PersonalExpenseDTO> {
    private final UserRepository userRepository;

    public PersonalExpensesConverter(CsvProcessor<PersonalExpenseDTO> csvProcessor, UserRepository userRepository) {
        super(csvProcessor);
        this.userRepository = userRepository;
    }

    @Override
    public PersonalExpense createFromDTO(PersonalExpenseDTO dto) {
        Optional<User> payer = userRepository.getUserByUsername(dto.payerUsername());
        if (payer.isEmpty()) {
            return null;
        }

        Optional<User> debtor = userRepository.getUserByUsername(dto.debtorUsername());
        if (debtor.isEmpty()) {
            return null;
        }

        return new PersonalExpense(payer.get(), debtor.get(), dto.amount(),
                dto.reason(), dto.timestamp());
    }
}

