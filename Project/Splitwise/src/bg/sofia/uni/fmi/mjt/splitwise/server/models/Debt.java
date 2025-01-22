package bg.sofia.uni.fmi.mjt.splitwise.server.models;

import java.util.Objects;

public class Debt {
    private User payer;
    private User receiver;
    private double amount;

    public Debt(User payer, User receiver, double amount) {
        this.payer = payer;
        this.receiver = receiver;
        this.amount = amount;
    }

    public User payer() {
        return payer;
    }

    public User receiver() {
        return receiver;
    }

    public double amount() {
        return amount;
    }

    public void updateAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount cannot be less than or equal to 0!");
        }
        this.amount = amount;
    }

    public void swapSides() {
        User temp = payer;
        payer = receiver;
        receiver = temp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Debt debt = (Debt) o;
        return Double.compare(amount, debt.amount) == 0 && Objects.equals(payer, debt.payer) && Objects.equals(receiver, debt.receiver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payer, receiver, amount);
    }
}
