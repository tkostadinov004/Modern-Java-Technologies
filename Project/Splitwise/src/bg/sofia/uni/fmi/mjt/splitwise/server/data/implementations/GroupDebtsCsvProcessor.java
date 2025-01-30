package bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.GroupDebtDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import com.opencsv.CSVReader;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class GroupDebtsCsvProcessor extends CsvProcessor<GroupDebtDTO> {
    private final UserRepository userRepository;
    private final FriendGroupRepository friendGroupRepository;

    public GroupDebtsCsvProcessor(UserRepository userRepository, FriendGroupRepository friendGroupRepository, CSVReader reader, String filePath) {
        super(reader, filePath);
        this.userRepository = userRepository;
        this.friendGroupRepository = friendGroupRepository;
    }

    private static final int DEBTOR_USERNAME_INDEX = 0;
    private static final int RECIPIENT_USERNAME_INDEX = 1;
    private static final int GROUP_INDEX = 2;
    private static final int AMOUNT_INDEX = 3;
    private static final int REASON_INDEX = 4;

    private GroupDebtDTO parseGroupDebt(String[] args) {
        Optional<User> debtor = userRepository.getUserByUsername(args[DEBTOR_USERNAME_INDEX]);
        if (debtor.isEmpty()) {
            return null;
        }

        Optional<User> recipient = userRepository.getUserByUsername(args[RECIPIENT_USERNAME_INDEX]);
        if (recipient.isEmpty()) {
            return null;
        }

        Optional<FriendGroup> group = friendGroupRepository.getGroup(args[GROUP_INDEX]);
        if (group.isEmpty()) {
            return null;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[AMOUNT_INDEX]);
        } catch (NumberFormatException e) {
            return null;
        }

        return new GroupDebtDTO(debtor.get(), recipient.get(), group.get(), amount, args[REASON_INDEX]);
    }

    @Override
    public Set<GroupDebtDTO> readAll() {
        return super.readAll(this::parseGroupDebt);
    }

    private String serializePersonalDebt(GroupDebtDTO debt) {
        return "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\""
                .formatted(debt.debtor().username(),
                        debt.recipient().username(),
                        debt.group().name(),
                        debt.amount(), debt.reason());
    }

    @Override
    public synchronized void writeToFile(GroupDebtDTO obj) {
        super.writeToFile(obj, this::serializePersonalDebt);
    }
}
