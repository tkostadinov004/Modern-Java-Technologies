package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Debt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.GroupDebtDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.dto.PersonalDebtDTO;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingGroupException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingUserException;

import java.time.LocalDateTime;
import java.util.HashMap;
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
    private final Map<FriendGroup, Set<Debt>> groupDebts;

    private Map<FriendGroup, Set<Debt>> populateDebts() {
        return csvProcessor
                .readAll()
                .stream()
                .collect(Collectors.groupingBy(debt -> debt.group(), Collectors.mapping(dto -> new Debt(dto.debtor(), dto.recipient(), dto.amount(), dto.reason()), Collectors.toSet())));
    }

    public DefaultGroupDebtsRepository(CsvProcessor<GroupDebtDTO> csvProcessor, UserRepository userRepository, FriendGroupRepository friendGroupRepository, NotificationsRepository notificationsRepository) {
        this.csvProcessor = csvProcessor;
        this.userRepository = userRepository;
        this.friendGroupRepository = friendGroupRepository;
        this.notificationsRepository = notificationsRepository;
        this.groupDebts = populateDebts();
    }

    @Override
    public Map<FriendGroup, Set<Debt>> getDebtsOf(String username) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(username));
        }

        return friendGroupRepository
                .getGroupsOf(username)
                .stream()
                .collect(Collectors.toMap(key -> key,
                        key -> {
                        if (!groupDebts.containsKey(key)) {
                            return Set.of();
                        }
                        return groupDebts.get(key).stream().filter(debt -> debt.debtor().equals(user.get()) || debt.recipient().equals(user.get())).collect(Collectors.toSet());
                        }));
    }

    private Optional<Debt> getDebtOfDebtorAndRecipient(User debtor, User recipient, FriendGroup group, String reason) {
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

    @Override
    public Optional<Debt> getDebtOfDebtorAndRecipient(String debtorUsername,
                                                      String recipientUsername,
                                                      String groupName,
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

        Optional<User> debtor = userRepository.getUserByUsername(debtorUsername);
        if (debtor.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(debtorUsername));
        }
        Optional<User> recipient = userRepository.getUserByUsername(recipientUsername);
        if (recipient.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(recipientUsername));
        }
        Optional<FriendGroup> friendGroup = friendGroupRepository.getGroup(groupName);
        if (friendGroup.isEmpty()) {
            throw new NonExistingGroupException("Group with name %s does not exist!".formatted(groupName));
        }

        return getDebtOfDebtorAndRecipient(debtor.get(), recipient.get(), friendGroup.get(), reason);
    }

    private void addDebt(User debtor, User recipient, FriendGroup group, double amount, String reason) {
        Debt debt = new Debt(debtor, recipient, amount, reason);
        groupDebts.putIfAbsent(group, new HashSet<>());
        groupDebts.get(group).add(debt);
        csvProcessor.writeToFile(new GroupDebtDTO(debtor, recipient, group, amount, reason));
    }

    @Override
    public void addDebt(String debtorUsername, String recipientUsername, String groupName, double amount, String reason) {
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

        Optional<User> debtor = userRepository.getUserByUsername(debtorUsername);
        if (debtor.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(debtorUsername));
        }
        Optional<User> recipient = userRepository.getUserByUsername(recipientUsername);
        if (recipient.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(recipientUsername));
        }
        Optional<FriendGroup> friendGroup = friendGroupRepository.getGroup(groupName);
        if (friendGroup.isEmpty()) {
            throw new NonExistingGroupException("Group with name %s does not exist!".formatted(groupName));
        }

        addDebt(debtor.get(), recipient.get(), friendGroup.get(), amount, reason);
    }

    @Override
    public void lowerDebtBurden(String debtorUsername, String recipientUsername, String groupName, double amount, String reason) {
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

        Optional<User> debtor = userRepository.getUserByUsername(debtorUsername);
        if (debtor.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(debtorUsername));
        }
        Optional<User> recipient = userRepository.getUserByUsername(recipientUsername);
        if (recipient.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(recipientUsername));
        }
        Optional<FriendGroup> friendGroup = friendGroupRepository.getGroup(groupName);
        if (friendGroup.isEmpty()) {
            throw new NonExistingGroupException("Group with name %s does not exist!".formatted(groupName));
        }

        Optional<Debt> debt = getDebtOfDebtorAndRecipient(debtor.get(), recipient.get(), friendGroup.get(), reason);
        if (debt.isEmpty()) {
            addDebt(debtor.get(), recipient.get(), friendGroup.get(), amount, reason);
            return;
        }

        double newAmount = debt.get().amount() - amount;
        if (newAmount == 0) {
            groupDebts.remove(debt.get());
            csvProcessor.remove(d -> d.debtor().username().equals(debtorUsername) && d.recipient().username().equals(recipientUsername) && d.group().name().equals(groupName) && d.reason().equals(reason));
        } else {
            debt.get().updateAmount(newAmount);
            csvProcessor.modify(d -> d.debtor().username().equals(debtorUsername) && d.recipient().username().equals(recipientUsername) && d.group().name().equals(groupName) && d.reason().equals(reason),
                    new GroupDebtDTO(debt.get().debtor(), debt.get().recipient(), friendGroup.get(), newAmount, debt.get().reason()));
        }
        notificationsRepository.addNotificationForUser(debtorUsername,
                "%s approved your payment of %s LV in group %s for %s. You now owe them %s LV.".formatted(recipientUsername, amount, groupName, reason, newAmount),
                LocalDateTime.now(),
                NotificationType.PERSONAL);
    }

    @Override
    public void increaseDebtBurden(String debtorUsername, String recipientUsername, String groupName, double amount, String reason) {
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

        Optional<User> debtor = userRepository.getUserByUsername(debtorUsername);
        if (debtor.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(debtorUsername));
        }
        Optional<User> recipient = userRepository.getUserByUsername(recipientUsername);
        if (recipient.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(recipientUsername));
        }
        Optional<FriendGroup> friendGroup = friendGroupRepository.getGroup(groupName);
        if (friendGroup.isEmpty()) {
            throw new NonExistingGroupException("Group with name %s does not exist!".formatted(groupName));
        }

        Optional<Debt> debt = getDebtOfDebtorAndRecipient(debtor.get(), recipient.get(), friendGroup.get(), reason);
        if (debt.isEmpty()) {
            addDebt(debtor.get(), recipient.get(), friendGroup.get(), amount, reason);
            return;
        }

        double newAmount = debt.get().amount() + amount;
        debt.get().updateAmount(newAmount);
        csvProcessor.modify(d -> d.debtor().username().equals(debtorUsername) && d.recipient().username().equals(recipientUsername) && d.group().name().equals(groupName) && d.reason().equals(reason),
                new GroupDebtDTO(debt.get().debtor(), debt.get().recipient(), friendGroup.get(), newAmount, debt.get().reason()));
    }
}
