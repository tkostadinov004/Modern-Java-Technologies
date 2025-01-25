package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.GroupDebt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.PersonalDebt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingGroupException;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistingUserException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultGroupDebtsRepository implements GroupDebtsRepository {
    private final UserRepository userRepository;
    private final FriendGroupRepository friendGroupRepository;
    private final Set<GroupDebt> groupDebts;

    public DefaultGroupDebtsRepository(UserRepository userRepository, FriendGroupRepository friendGroupRepository) {
        this.userRepository = userRepository;
        this.friendGroupRepository = friendGroupRepository;
        this.groupDebts = new HashSet<>();
    }

    @Override
    public Set<GroupDebt> getDebtsOf(String username, String groupName) {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null, blank or empty!");
        }
        if (groupName == null || groupName.isEmpty() || groupName.isBlank()) {
            throw new IllegalArgumentException("Group name cannot be null, blank or empty!");
        }

        Optional<User> user = userRepository.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new NonExistingUserException("User with username %s does not exist!".formatted(username));
        }

        Optional<FriendGroup> friendGroup = friendGroupRepository.getGroup(groupName);
        if (friendGroup.isEmpty()) {
            throw new NonExistingGroupException("Group with name %s does not exist!".formatted(groupName));
        }

        return groupDebts
                .stream()
                .filter(debt -> (debt.debtor().equals(user.get()) || debt.recipient().equals(user.get()))
                && debt.group().equals(friendGroup.get()))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Optional<GroupDebt> getDebtOfDebtorAndRecipient(User debtor, User recipient, FriendGroup group, String reason) {
        return groupDebts
                .stream()
                .filter(debt ->
                        ((debt.debtor().equals(debtor) && debt.recipient().equals(recipient)) ||
                                (debt.recipient().equals(debtor) && debt.debtor().equals(recipient))) &&
                                group.equals(debt.group()) &&
                                reason.equals(debt.reason()))
                .findFirst();
    }

    @Override
    public Optional<GroupDebt> getDebtOfDebtorAndRecipient(String debtorUsername,
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
        GroupDebt groupDebt = new GroupDebt(debtor, recipient, group, amount, reason);
        groupDebts.add(groupDebt);
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

    private void updateDebt(User debtor, User recipient, FriendGroup group, double amount, String reason) {
        Optional<GroupDebt> debt = getDebtOfDebtorAndRecipient(debtor, recipient, group, reason);
        if (debt.isEmpty()) {
            addDebt(debtor, recipient, group, amount, reason);
            return;
        }

        double newAmount = debt.get().amount() +
                (debt.get().debtor().equals(debtor) ? amount : -amount);
        if (newAmount < 0) {
            debt.get().swapSides();
            newAmount *= (-1);
        }
        debt.get().updateAmount(newAmount);
    }

    @Override
    public void updateDebt(String debtorUsername, String recipientUsername, String groupName, double amount, String reason) {
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

        updateDebt(debtor.get(), recipient.get(), friendGroup.get(), amount, reason);
    }
}
