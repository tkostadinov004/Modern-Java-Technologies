package bg.sofia.uni.fmi.mjt.frauddetector.rule;

import bg.sofia.uni.fmi.mjt.frauddetector.transaction.Transaction;

import java.util.List;

public class ZScoreRule implements Rule {
    private double zScoreThreshold;
    private double weight;

    public ZScoreRule(double zScoreThreshold, double weight) {
        this.zScoreThreshold = zScoreThreshold;
        this.weight = weight;
    }

    private double getAverage(List<Transaction> transactions) {
        return transactions
                .stream()
                .mapToDouble(t -> t.transactionAmount())
                .average()
                .getAsDouble();
    }

    private double calculateVariance(List<Transaction> transactions) {
        double average = getAverage(transactions);

        double variance = transactions
                .stream()
                .mapToDouble(t -> Math.pow(t.transactionAmount() - average, 2))
                .sum() / transactions.size();
        return variance;
    }

    private double getStandardDeviation(List<Transaction> transactions) {
        return Math.sqrt(calculateVariance(transactions));
    }

    private double getZScore(Transaction transaction, List<Transaction> transactions) {
        return (transaction.transactionAmount() - getAverage(transactions)) / getStandardDeviation(transactions);
    }

    @Override
    public boolean applicable(List<Transaction> transactions) {
        return transactions
                .stream()
                .anyMatch(t -> getZScore(t, transactions) > zScoreThreshold);
    }

    @Override
    public double weight() {
        return weight;
    }
}
