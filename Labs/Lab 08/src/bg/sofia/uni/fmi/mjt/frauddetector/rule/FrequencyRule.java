package bg.sofia.uni.fmi.mjt.frauddetector.rule;

import bg.sofia.uni.fmi.mjt.frauddetector.transaction.Transaction;

import java.time.temporal.TemporalAmount;
import java.util.List;

public class FrequencyRule implements Rule {
    private int transactionCountThreshold;
    private TemporalAmount timeWindow;
    private double weight;

    public FrequencyRule(int transactionCountThreshold, TemporalAmount timeWindow, double weight) {
        this.transactionCountThreshold = transactionCountThreshold;
        this.timeWindow = timeWindow;
        this.weight = weight;
    }

    @Override
    public boolean applicable(List<Transaction> transactions) {
        List<Transaction> sorted = transactions
                .stream()
                .sorted((a, b) -> a.transactionDate().compareTo(b.transactionDate()))
                .toList();
        return sorted
                .stream()
                .anyMatch(start -> {
                    return sorted
                            .stream()
                            .filter(end ->
                            !end.transactionDate().isBefore(start.transactionDate()) &&
                            !end.transactionDate().isAfter(start.transactionDate().plus(timeWindow)))
                            .count() >= transactionCountThreshold;
                });
    }

    @Override
    public double weight() {
        return weight;
    }
}
