package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.GroupDebt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.GroupDebtDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GroupDebtsConverter
        extends DataConverter<Map<FriendGroup, Set<GroupDebt>>, GroupDebt, GroupDebtDTO> {
    private final UserRepository userRepository;
    private final FriendGroupRepository friendGroupRepository;

    public GroupDebtsConverter(CsvProcessor<GroupDebtDTO> csvProcessor,
                               UserRepository userRepository,
                               FriendGroupRepository friendGroupRepository) {
        super(csvProcessor);
        this.userRepository = userRepository;
        this.friendGroupRepository = friendGroupRepository;
    }

    @Override
    public GroupDebt createFromDTO(GroupDebtDTO dto) {
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

        Optional<FriendGroup> group = friendGroupRepository.getGroup(dto.groupName());
        if (group.isEmpty()) {
            return null;
        }
        if (!group.get().participants().contains(debtor.get()) ||
                !group.get().participants().contains(recipient.get())) {
            return null;
        }
        return new GroupDebt(debtor.get(), recipient.get(), group.get(), dto.amount(), dto.reason());
    }
}
