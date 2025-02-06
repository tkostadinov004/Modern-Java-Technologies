package bg.sofia.uni.fmi.mjt.splitwise.server.models;

import java.util.Objects;

public class GroupDebt {
    private User debtor;
    private User recipient;
    private FriendGroup group;
    private double amount;
    private String reason;

    public GroupDebt(User debtor, User recipient, FriendGroup group, double amount, String reason) {
        this.debtor = debtor;
        this.recipient = recipient;
        this.group = group;
        this.amount = amount;
        this.reason = reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupDebt groupDebt = (GroupDebt) o;
        return Double.compare(amount, groupDebt.amount) == 0 && Objects.equals(debtor, groupDebt.debtor) && Objects.equals(recipient, groupDebt.recipient) && Objects.equals(group, groupDebt.group) && Objects.equals(reason, groupDebt.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(debtor, recipient, group, amount, reason);
    }

    public User debtor() {
        return debtor;
    }

    public User recipient() {
        return recipient;
    }

    public FriendGroup group() {
        return group;
    }

    public double amount() {
        return amount;
    }

    public String reason() {
        return reason;
    }

    public void updateAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount cannot be less than or equal to 0!");
        }
        this.amount = amount;
    }
}

