package bg.sofia.uni.fmi.mjt.splitwise.server.models;

public class PersonalDebt {
    private User debtor;
    private User recipient;
    private double amount;
    private String reason;

    public PersonalDebt(User debtor, User recipient, double amount, String reason) {
        this.debtor = debtor;
        this.recipient = recipient;
        this.amount = amount;
        this.reason = reason;
    }

    public User debtor() {
        return debtor;
    }

    public User recipient() {
        return recipient;
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
