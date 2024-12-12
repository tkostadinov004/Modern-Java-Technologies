package bg.sofia.uni.fmi.mjt.frauddetector.rule;

import bg.sofia.uni.fmi.mjt.frauddetector.transaction.Channel;
import bg.sofia.uni.fmi.mjt.frauddetector.transaction.Transaction;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocationsRuleTest {
    @Test
    void applicableCorrectly() {
        Rule rule = new LocationsRule(4, 0.3);
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("TX000002",
                "AC00128",
                14.09,
                LocalDateTime.of(2023, 4, 11, 16, 29, 14),
                "San Diego",
                Channel.ATM));
        transactions.add(new Transaction("TX000003",
                "SD20231",
                16.10,
                LocalDateTime.of(2023, 5, 12, 16, 29, 14),
                "San Diego",
                Channel.ATM));
        transactions.add(new Transaction("TX000006",
                "SD20232",
                200,
                LocalDateTime.of(2023, 5, 16, 16, 29, 14),
                "Sofia",
                Channel.BRANCH));
        transactions.add(new Transaction("TX000007",
                "SD20233",
                19.99,
                LocalDateTime.of(2023, 5, 16, 16, 29, 14),
                "Burgas",
                Channel.ONLINE));

        assertEquals(false, rule.applicable(transactions));
        transactions.add(new Transaction("TX000081",
                "SD20236",
                19.99,
                LocalDateTime.of(2023, 5, 16, 16, 29, 14),
                "Kowloon, HK",
                Channel.BRANCH));
        assertEquals(true, rule.applicable(transactions));
    }
}
