package bg.sofia.uni.fmi.mjt.frauddetector.analyzer;

import bg.sofia.uni.fmi.mjt.frauddetector.rule.FrequencyRule;
import bg.sofia.uni.fmi.mjt.frauddetector.rule.LocationsRule;
import bg.sofia.uni.fmi.mjt.frauddetector.rule.Rule;
import bg.sofia.uni.fmi.mjt.frauddetector.rule.SmallTransactionsRule;
import bg.sofia.uni.fmi.mjt.frauddetector.rule.ZScoreRule;
import bg.sofia.uni.fmi.mjt.frauddetector.transaction.Channel;
import bg.sofia.uni.fmi.mjt.frauddetector.transaction.Transaction;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransactionAnalyzerImplTest {
    private StringReader reader = new StringReader(
            System.lineSeparator() +
            "TX000001,AC00128,14.09,2023-04-11 16:29:14,San Diego,ATM"
            + System.lineSeparator()
            + "TX000002,AC00455,376.24,2023-06-27 16:44:19,Houston,ATM"
            + System.lineSeparator()
            + "TX000003,AC00455,200,2023-06-27 16:45:19,Houston,Online"
            + System.lineSeparator()
            + "TX000004,AC00455,220,2023-06-27 17:45:19,Burgas,Branch"
            + System.lineSeparator()
            + "TX000005,AC00457,100,2023-06-27 17:47:19,Varna,Branch"
            + System.lineSeparator());
    private List<Rule> rules = List.of(
            new ZScoreRule(1.5, 0.3),
            new LocationsRule(3, 0.4),
            new FrequencyRule(2, Period.ofWeeks(4), 0.25),
            new SmallTransactionsRule(1, 100.20, 0.05)
    );
    @Test
    public void throwsOnInvalidWeightSum() {
        List<Rule> rules = List.of(
                new ZScoreRule(1.5, 0.3),
                new LocationsRule(3, 0.4),
                new FrequencyRule(4, Period.ofWeeks(4), 0.35),
                new SmallTransactionsRule(1, 10.20, 0.05)
        );
        assertThrows(IllegalArgumentException.class, () ->
                new TransactionAnalyzerImpl(reader, rules),
                "Rule weight sum shouldn't be over 1.0!");
    }
    @Test
    public void returnsAllTransactions() {
        List<Transaction> expected = new ArrayList<>();
        expected.add(new Transaction("TX000001",
                "AC00128",
                14.09,
                LocalDateTime.of(2023, 4, 11, 16, 29, 14),
                "San Diego",
                Channel.ATM));
        expected.add(new Transaction("TX000002",
                "AC00455",
                376.24,
                LocalDateTime.of(2023, 6, 27, 16, 44, 19),
                "Houston",
                Channel.ATM));
        expected.add(new Transaction("TX000003",
                "AC00455",
                200,
                LocalDateTime.of(2023, 6, 27, 16, 45, 19),
                "Houston",
                Channel.ONLINE));
        expected.add(new Transaction("TX000004",
                "AC00455",
                220,
                LocalDateTime.of(2023, 6, 27, 17, 45, 19),
                "Burgas",
                Channel.BRANCH));
        expected.add(new Transaction("TX000005",
                "AC00457",
                100,
                LocalDateTime.of(2023, 6, 27, 17, 47, 19),
                "Varna",
                Channel.BRANCH));

        TransactionAnalyzer transactionAnalyzer = new TransactionAnalyzerImpl(reader, rules);
        assertIterableEquals(expected, transactionAnalyzer.allTransactions());
    }
    @Test
    public void returnsAccountIDs() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("TX000001",
                "AC00128",
                14.09,
                LocalDateTime.of(2023, 4, 11, 16, 29, 14),
                "San Diego",
                Channel.ATM));
        transactions.add(new Transaction("TX000002",
                "AC00455",
                376.24,
                LocalDateTime.of(2023, 6, 27, 16, 44, 19),
                "Houston",
                Channel.ATM));
        transactions.add(new Transaction("TX000003",
                "AC00455",
                200,
                LocalDateTime.of(2023, 6, 27, 16, 45, 19),
                "Houston",
                Channel.ONLINE));
        transactions.add(new Transaction("TX000004",
                "AC00455",
                220,
                LocalDateTime.of(2023, 6, 27, 17, 45, 19),
                "Burgas",
                Channel.BRANCH));
        transactions.add(new Transaction("TX000005",
                "AC00457",
                100,
                LocalDateTime.of(2023, 6, 27, 17, 47, 19),
                "Varna",
                Channel.BRANCH));

        TransactionAnalyzer transactionAnalyzer = new TransactionAnalyzerImpl(reader, rules);
        List<String> accountIDs = transactionAnalyzer.allAccountIDs();
        assertIterableEquals(accountIDs, List.of("AC00128", "AC00455", "AC00457"));
    }
    @Test
    public void returnsAccountIDsWhenEmpty() {
        TransactionAnalyzer transactionAnalyzer = new TransactionAnalyzerImpl(new StringReader(""), rules);
        List<String> accountIDs = transactionAnalyzer.allAccountIDs();
        assertIterableEquals(accountIDs, List.of());
    }
    @Test
    public void returnsTransactionCountsByChannel() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("TX000001",
                "AC00128",
                14.09,
                LocalDateTime.of(2023, 4, 11, 16, 29, 14),
                "San Diego",
                Channel.ATM));
        transactions.add(new Transaction("TX000002",
                "AC00455",
                376.24,
                LocalDateTime.of(2023, 6, 27, 16, 44, 19),
                "Houston",
                Channel.ATM));
        transactions.add(new Transaction("TX000003",
                "AC00455",
                200,
                LocalDateTime.of(2023, 6, 27, 16, 45, 19),
                "Houston",
                Channel.ONLINE));
        transactions.add(new Transaction("TX000004",
                "AC00455",
                220,
                LocalDateTime.of(2023, 6, 27, 17, 45, 19),
                "Burgas",
                Channel.BRANCH));
        transactions.add(new Transaction("TX000005",
                "AC00457",
                100,
                LocalDateTime.of(2023, 6, 27, 17, 47, 19),
                "Varna",
                Channel.BRANCH));

        TransactionAnalyzer transactionAnalyzer = new TransactionAnalyzerImpl(reader, rules);
        var map = transactionAnalyzer.transactionCountByChannel();
        assertEquals(2, map.get(Channel.ATM));
        assertEquals(1, map.get(Channel.ONLINE));
        assertEquals(2, map.get(Channel.BRANCH));
    }
    @Test
    public void getAmountThrowsOnInvalidUser() {
        TransactionAnalyzer transactionAnalyzer = new TransactionAnalyzerImpl(reader, rules);
        assertThrows(IllegalArgumentException.class, () -> transactionAnalyzer.amountSpentByUser(""));
        assertThrows(IllegalArgumentException.class, () -> transactionAnalyzer.amountSpentByUser(null));
    }
    @Test
    public void getsAmountByUserCorrectly() {
        TransactionAnalyzer transactionAnalyzer = new TransactionAnalyzerImpl(reader, rules);
        assertEquals(14.09, transactionAnalyzer.amountSpentByUser("AC00128"));
        assertEquals(376.24 + 200 + 220, transactionAnalyzer.amountSpentByUser("AC00455"));
        assertEquals(100, transactionAnalyzer.amountSpentByUser("AC00457"));
    }
    @Test
    public void getTransactionsThrowsOnInvalidUser() {
        TransactionAnalyzer transactionAnalyzer = new TransactionAnalyzerImpl(reader, rules);
        assertThrows(IllegalArgumentException.class, () -> transactionAnalyzer.allTransactionsByUser(""));
        assertThrows(IllegalArgumentException.class, () -> transactionAnalyzer.allTransactionsByUser(null));
    }
    @Test
    public void getsTransactionsByUserCorrectly() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("TX000001",
                "AC00128",
                14.09,
                LocalDateTime.of(2023, 4, 11, 16, 29, 14),
                "San Diego",
                Channel.ATM));
        transactions.add(new Transaction("TX000002",
                "AC00455",
                376.24,
                LocalDateTime.of(2023, 6, 27, 16, 44, 19),
                "Houston",
                Channel.ATM));
        transactions.add(new Transaction("TX000003",
                "AC00455",
                200,
                LocalDateTime.of(2023, 6, 27, 16, 45, 19),
                "Houston",
                Channel.ONLINE));
        transactions.add(new Transaction("TX000004",
                "AC00455",
                220,
                LocalDateTime.of(2023, 6, 27, 17, 45, 19),
                "Burgas",
                Channel.BRANCH));
        transactions.add(new Transaction("TX000005",
                "AC00457",
                100,
                LocalDateTime.of(2023, 6, 27, 17, 47, 19),
                "Varna",
                Channel.BRANCH));

        TransactionAnalyzer transactionAnalyzer = new TransactionAnalyzerImpl(reader, rules);
        assertIterableEquals(List.of(transactions.get(0)), transactionAnalyzer.allTransactionsByUser("AC00128"));
        assertIterableEquals(List.of(transactions.get(1), transactions.get(2), transactions.get(3)), transactionAnalyzer.allTransactionsByUser("AC00455"));
        assertIterableEquals(List.of(transactions.get(4)), transactionAnalyzer.allTransactionsByUser("AC00457"));
    }
    @Test
    public void getRatingThrowsOnInvalidUser() {
        TransactionAnalyzer transactionAnalyzer = new TransactionAnalyzerImpl(reader, rules);
        assertThrows(IllegalArgumentException.class, () -> transactionAnalyzer.accountRating(""));
        assertThrows(IllegalArgumentException.class, () -> transactionAnalyzer.accountRating(null));
    }
    @Test
    public void getsRatingByUserCorrectly() {
        TransactionAnalyzer transactionAnalyzer = new TransactionAnalyzerImpl(reader, rules);
        assertEquals(0.05, transactionAnalyzer.accountRating("AC00128"));
        assertEquals(0.25, transactionAnalyzer.accountRating("AC00455"));
        assertEquals(0.05, transactionAnalyzer.accountRating("AC00457"));
    }
    @Test
    public void getsRisksCorrectly() {
        TransactionAnalyzer transactionAnalyzer = new TransactionAnalyzerImpl(reader, rules);
        SortedMap<String, Double> map = transactionAnalyzer.accountsRisk();
        assertTrue(map.firstEntry().getKey().equals("AC00128") && Double.compare(0.05, map.firstEntry().getValue()) == 0);
        map.pollFirstEntry();
        assertTrue(map.firstEntry().getKey().equals("AC00455") && Double.compare(0.25, map.firstEntry().getValue()) == 0);
        map.pollFirstEntry();
        assertTrue(map.firstEntry().getKey().equals("AC00457") && Double.compare(0.05, map.firstEntry().getValue()) == 0);
    }
}
