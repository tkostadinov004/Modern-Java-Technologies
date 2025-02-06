package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.GroupDebtsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.GroupDebt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.GroupDebtDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentDebtException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingGroupException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentUserException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter.DataConverter;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter.GroupDebtsConverter;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultGroupDebtsRepository implements GroupDebtsRepository {
    private final CsvProcessor<GroupDebtDTO> csvProcessor;
    private final UserRepository userRepository;
    private final FriendGroupRepository friendGroupRepository;
    private final NotificationsRepository notificationsRepository;
    private final Map<FriendGroup, Set<GroupDebt>> groupDebts;

    public DefaultGroupDebtsRepository(DependencyContainer dependencyContainer) {
        this.csvProcessor = dependencyContainer.get(GroupDebtsCsvProcessor.class);
        this.userRepository = dependencyContainer.get(UserRepository.class);
        this.friendGroupRepository = dependencyContainer.get(FriendGroupRepository.class);
        this.notificationsRepository = dependencyContainer.get(NotificationsRepository.class);

        DataConverter<Map<FriendGroup, Set<GroupDebt>>, GroupDebt, GroupDebtDTO> converter =
                new GroupDebtsConverter(csvProcessor, userRepository, friendGroupRepository);
        this.groupDebts = converter.populate(Collectors.groupingBy(GroupDebt::group,
                Collectors.mapping(debt -> debt, Collectors.toSet())));
    }

    @Override
    public Map<FriendGroup, Set<GroupDebt>> getDebtsOf(String username) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(username));
        }

        return friendGroupRepository
                .getGroupsOf(username)
                .stream()
                .collect(Collectors.toMap(key -> key,
                        key -> {
                        if (!groupDebts.containsKey(key)) {
                            return Set.of();
                        }
                        return groupDebts
                                .get(key)
                                .stream()
                                .filter(debt -> debt.debtor().equals(user.get()) ||
                                        debt.recipient().equals(user.get())).collect(Collectors.toSet());
                        }));
    }

    private Optional<GroupDebt> getDebtOfDebtorAndRecipient(User debtor,
                                                            User recipient,
                                                            FriendGroup group,
                                                            String reason) {
        if (!groupDebts.containsKey(group)) {
            return Optional.empty();
        }

        return groupDebts.get(group)
                .stream()
                .filter(debt ->
                        ((debt.debtor().equals(debtor) && debt.recipient().equals(recipient)) ||
                                (debt.recipient().equals(debtor) && debt.debtor().equals(recipient))) &&
                                reason.equals(debt.reason()))
                .findFirst();
    }

    private void addDebt(User debtor, User recipient, FriendGroup group, double amount, String reason) {
        GroupDebt debt = new GroupDebt(debtor, recipient, group, amount, reason);
        groupDebts.putIfAbsent(group, new HashSet<>());
        groupDebts.get(group).add(debt);
        csvProcessor.writeToFile(
                new GroupDebtDTO(debtor.username(), recipient.username(), group.name(), amount, reason));
    }

    private void lowerDebtBurden(GroupDebt debt, FriendGroup group, double amount) {
        double newAmount = debt.amount() - amount;

        GroupDebtDTO crudDTO = new GroupDebtDTO(debt.debtor().username(),
                debt.recipient().username(), group.name(), debt.amount(), debt.reason());

        if (newAmount <= 0) {
            groupDebts.get(group).remove(debt);
            csvProcessor.remove(crudDTO);
        } else {
            debt.updateAmount(newAmount);

            GroupDebtDTO updatedDebtDTO = new GroupDebtDTO(debt.debtor().username(),
                    debt.recipient().username(),
                    debt.group().name(),
                    newAmount,
                    debt.reason());

            csvProcessor.modify(crudDTO, updatedDebtDTO);
        }
        notificationsRepository.addNotificationForUser(debt.debtor().username(),
                "%s approved your payment of %s LV in group %s for %s. You now owe them %s LV."
                        .formatted(debt.recipient().username(), amount, debt.group().name(), debt.reason(), newAmount),
                NotificationType.PERSONAL);
    }

    private void validateArguments(String debtorUsername,
                                   String recipientUsername,
                                   String groupName,
                                   double amount,
                                   String reason) {
        if (debtorUsername == null || debtorUsername.isEmpty() || debtorUsername.isBlank()) {
            throw new IllegalArgumentException("Debtor username cannot be null, blank or empty!");
        }
        if (recipientUsername == null || recipientUsername.isEmpty() || recipientUsername.isBlank()) {
            throw new IllegalArgumentException("Recipient username cannot be null, blank or empty!");
        }
        if (groupName == null || groupName.isEmpty() || groupName.isBlank()) {
            throw new IllegalArgumentException("Group name cannot be null, blank or empty!");
        }
        if (reason == null || reason.isEmpty() || reason.isBlank()) {
            throw new IllegalArgumentException("Reason cannot be null, blank or empty!");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Debt amount cannot be less than or equal to 0!");
        }
        if (debtorUsername.equals(recipientUsername)) {
            throw new IllegalArgumentException("Debtor and recipient cannot be the same person!");
        }
    }

    @Override
    public void lowerDebtBurden(String debtorUsername,
                                String recipientUsername,
                                String groupName,
                                double amount,
                                String reason) {
        validateArguments(debtorUsername, recipientUsername, groupName, amount, reason);

        Optional<User> debtor = userRepository.getUserByUsername(debtorUsername);
        if (debtor.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(debtorUsername));
        }
        Optional<User> recipient = userRepository.getUserByUsername(recipientUsername);
        if (recipient.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(recipientUsername));
        }
        Optional<FriendGroup> friendGroup = friendGroupRepository.getGroup(groupName);
        if (friendGroup.isEmpty()) {
            throw new NonExistingGroupException("Group with name %s does not exist!".formatted(groupName));
        }
        Optional<GroupDebt> debt =
                getDebtOfDebtorAndRecipient(debtor.get(), recipient.get(), friendGroup.get(), reason);
        if (debt.isEmpty()) {
            throw new NonExistentDebtException("Debt in group \"%s\" with debtor %s and reason \"%s\" doesn't exist!"
                    .formatted(groupName, debtorUsername, reason));
        }

        lowerDebtBurden(debt.get(), friendGroup.get(), amount);
    }

    private void increaseDebtBurden(GroupDebt debt, FriendGroup group, double amount) {
        double newAmount = debt.amount() + amount;
        GroupDebtDTO crudDTO = new GroupDebtDTO(debt.debtor().username(),
                debt.recipient().username(), group.name(), debt.amount(), debt.reason());

        debt.updateAmount(newAmount);

        GroupDebtDTO updatedDebtDTO = new GroupDebtDTO(debt.debtor().username(),
                debt.recipient().username(),
                debt.group().name(),
                newAmount,
                debt.reason());

        csvProcessor.modify(crudDTO, updatedDebtDTO);
    }

    @Override
    public void increaseDebtBurden(String debtorUsername,
                                   String recipientUsername,
                                   String groupName,
                                   double amount,
                                   String reason) {
        validateArguments(debtorUsername, recipientUsername, groupName, amount, reason);

        Optional<User> debtor = userRepository.getUserByUsername(debtorUsername);
        if (debtor.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(debtorUsername));
        }
        Optional<User> recipient = userRepository.getUserByUsername(recipientUsername);
        if (recipient.isEmpty()) {
            throw new NonExistentUserException("User with username %s does not exist!".formatted(recipientUsername));
        }
        Optional<FriendGroup> friendGroup = friendGroupRepository.getGroup(groupName);
        if (friendGroup.isEmpty()) {
            throw new NonExistingGroupException("Group with name %s does not exist!".formatted(groupName));
        }
        Optional<GroupDebt> debt =
                getDebtOfDebtorAndRecipient(debtor.get(), recipient.get(), friendGroup.get(), reason);
        if (debt.isEmpty()) {
            addDebt(debtor.get(), recipient.get(), friendGroup.get(), amount, reason);
            return;
        }

        increaseDebtBurden(debt.get(), friendGroup.get(), amount);
    }
}
