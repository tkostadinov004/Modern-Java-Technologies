package bg.sofia.uni.fmi.mjt.frauddetector.rule;

import bg.sofia.uni.fmi.mjt.frauddetector.transaction.Channel;
import bg.sofia.uni.fmi.mjt.frauddetector.transaction.Transaction;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SmallTransactionsRuleTest {
    @Test
    void applicableCorrectly() {
        Rule rule = new SmallTransactionsRule(3, 20, 0.2);
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("TX000001",
                "AC00128",
                14.09,
                LocalDateTime.of(2023, 4, 11, 16, 29, 14),
                "San Diego",
                Channel.ATM));
        transactions.add(new Transaction("TX000002",
                "SD20231",
                16.10,
                LocalDateTime.of(2023, 5, 12, 16, 29, 14),
                "San Diego",
                Channel.ATM));
        transactions.add(new Transaction("TX000003",
                "SD20232",
                200,
                LocalDateTime.of(2023, 5, 16, 16, 29, 14),
                "Sofia",
                Channel.BRANCH));
        assertEquals(false, rule.applicable(transactions));
        transactions.add(new Transaction("TX000004",
                "SD20233",
                19.99,
                LocalDateTime.of(2023, 5, 16, 16, 29, 14),
                "Burgas",
                Channel.ONLINE));

        assertEquals(true, rule.applicable(transactions));
    }
}
