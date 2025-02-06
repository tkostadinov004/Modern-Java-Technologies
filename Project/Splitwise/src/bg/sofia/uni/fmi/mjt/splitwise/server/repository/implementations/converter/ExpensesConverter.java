package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Expense;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.ExpenseDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ExpensesConverter
        extends DataConverter<Map<User, Set<Expense>>, Expense, ExpenseDTO> {
    private final UserRepository userRepository;

    public ExpensesConverter(CsvProcessor<ExpenseDTO> csvProcessor, UserRepository userRepository) {
        super(csvProcessor);
        this.userRepository = userRepository;
    }

    @Override
    public Expense createFromDTO(ExpenseDTO dto) {
        Optional<User> payer = userRepository.getUserByUsername(dto.payerUsername());
        if (payer.isEmpty()) {
            return null;
        }
        try {
            Set<User> participants = dto
                    .participantsUsernames()
                    .stream().map(username -> {
                        Optional<User> user = userRepository.getUserByUsername(username);
                        if (user.isEmpty()) {
                            throw new IllegalArgumentException();
                        }
                        return user.get();
                    }).collect(Collectors.toSet());
            return new Expense(payer.get(), dto.amount(), dto.reason(), dto.timestamp(), participants);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

