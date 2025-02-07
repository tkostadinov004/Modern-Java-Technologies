package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.GroupExpense;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.GroupExpenseDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GroupExpensesConverter
        extends DataConverter<Map<User, Set<GroupExpense>>, GroupExpense, GroupExpenseDTO> {
    private final UserRepository userRepository;
    private final FriendGroupRepository groupRepository;

    public GroupExpensesConverter(CsvProcessor<GroupExpenseDTO> csvProcessor,
                                  UserRepository userRepository,
                                  FriendGroupRepository groupRepository) {
        super(csvProcessor);
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    public GroupExpense createFromDTO(GroupExpenseDTO dto) {
        Optional<User> payer = userRepository.getUserByUsername(dto.payerUsername());
        if (payer.isEmpty()) {
            return null;
        }

        Optional<FriendGroup> group = groupRepository.getGroup(dto.groupName());
        if (group.isEmpty()) {
            return null;
        }

        return new GroupExpense(payer.get(), dto.amount(), dto.reason(), group.get(),
                dto.timestamp());
    }
}
