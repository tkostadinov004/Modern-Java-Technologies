package bg.sofia.uni.fmi.mjt.splitwise.server.models;

import java.util.Objects;

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

    public void swapSides() {
        User temp = debtor;
        debtor = recipient;
        recipient = temp;
    }

    public void updateAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount cannot be less than or equal to 0!");
        }
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonalDebt that = (PersonalDebt) o;
        return Double.compare(amount, that.amount) == 0 && Objects.equals(debtor, that.debtor) && Objects.equals(recipient, that.recipient) && Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(debtor, recipient, amount, reason);
    }
}
