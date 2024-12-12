package bg.sofia.uni.fmi.mjt.frauddetector.analyzer;

import bg.sofia.uni.fmi.mjt.frauddetector.rule.Rule;
import bg.sofia.uni.fmi.mjt.frauddetector.transaction.Channel;
import bg.sofia.uni.fmi.mjt.frauddetector.transaction.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class TransactionAnalyzerImpl implements TransactionAnalyzer {
    private List<Rule> rules;
    private List<Transaction> transactions;

    public TransactionAnalyzerImpl(Reader reader, List<Rule> rules) {
        transactions = new ArrayList<>();
        setRules(rules);
        readRecords(reader);
    }

    private void readRecords(Reader reader) {
        try (BufferedReader br = new BufferedReader(reader)) {
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                transactions.add(Transaction.of(line));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setRules(List<Rule> rules) {
        double sumWeight = rules
                .stream()
                .mapToDouble(r -> r.weight())
                .sum();
        if (Double.compare(sumWeight, 1.0) != 0) {
            throw new IllegalArgumentException("The sum of rule weights should be 1.0!");
        }
        this.rules = rules;
    }

    @Override
    public List<Transaction> allTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    @Override
    public List<String> allAccountIDs() {
        return transactions
                .stream()
                .map(t -> t.accountID())
                .distinct()
                .toList();
    }

    @Override
    public Map<Channel, Integer> transactionCountByChannel() {
        return transactions
                .stream()
                .collect(Collectors.groupingBy(Transaction::channel, Collectors.counting()))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().intValue()));
    }

    @Override
    public double amountSpentByUser(String accountID) {
        if (accountID == null || accountID.isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty!");
        }
        return transactions
                .stream()
                .filter(t -> t.accountID().equals(accountID))
                .collect(Collectors.summingDouble(t -> t.transactionAmount()));
    }

    @Override
    public List<Transaction> allTransactionsByUser(String accountId) {
        if (accountId == null || accountId.isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty!");
        }
        return transactions
                .stream()
                .filter(t -> t.accountID().equals(accountId))
                .toList();
    }

    @Override
    public double accountRating(String accountId) {
        if (accountId == null || accountId.isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty!");
        }

        List<Transaction> userTransactions = allTransactionsByUser(accountId);
        return rules
                .stream()
                .filter(r -> r.applicable(userTransactions))
                .collect(Collectors.summingDouble(r -> r.weight()));
    }

    @Override
    public SortedMap<String, Double> accountsRisk() {
        return transactions
                .stream()
                .collect(Collectors.groupingBy(Transaction::accountID))
                .keySet()
                .stream()
                .collect(Collectors.toMap(e -> e, e -> accountRating(e), (a, b) -> b, () -> new TreeMap<>()));
    }
}
