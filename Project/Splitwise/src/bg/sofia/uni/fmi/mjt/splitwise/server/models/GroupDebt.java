package bg.sofia.uni.fmi.mjt.splitwise.server.models;

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

    public void swapSides() {
        User temp = debtor;
        debtor = recipient;
        recipient = temp;
    }
}

