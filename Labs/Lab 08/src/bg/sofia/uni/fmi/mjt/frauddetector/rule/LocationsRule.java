package bg.sofia.uni.fmi.mjt.frauddetector.rule;

import bg.sofia.uni.fmi.mjt.frauddetector.transaction.Transaction;

import java.util.List;
import java.util.stream.Collectors;

public class LocationsRule implements Rule {
    private int threshold;
    private double weight;

    public LocationsRule(int threshold, double weight) {
        this.threshold = threshold;
        this.weight = weight;
    }

    @Override
    public boolean applicable(List<Transaction> transactions) {
        return transactions
                .stream()
                .collect(Collectors.groupingBy(t -> t.location()))
                .size() >= threshold;
    }

    @Override
    public double weight() {
        return weight;
    }
}
