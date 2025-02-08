package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.PersonalDebt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.PersonalDebtDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;

import java.util.Optional;
import java.util.Set;

public class PersonalDebtsConverter
        extends DataConverter<Set<PersonalDebt>, PersonalDebt, PersonalDebtDTO> {
    private final UserRepository userRepository;

    public PersonalDebtsConverter(CsvProcessor<PersonalDebtDTO> csvProcessor, UserRepository userRepository) {
        super(csvProcessor);
        this.userRepository = userRepository;
    }

    @Override
    public PersonalDebt createFromDTO(PersonalDebtDTO dto) {
        Optional<User> debtor = userRepository.getUserByUsername(dto.debtorUsername());
        if (debtor.isEmpty()) {
            return null;
        }
        Optional<User> recipient = userRepository.getUserByUsername(dto.recipientUsername());
        if (recipient.isEmpty()) {
            return null;
        }
        if (debtor.get().equals(recipient.get())) {
            return null;
        }
        return new PersonalDebt(debtor.get(), recipient.get(), dto.amount(), dto.reason());
    }
}
