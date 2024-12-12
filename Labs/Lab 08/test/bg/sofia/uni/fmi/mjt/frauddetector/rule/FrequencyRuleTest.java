package bg.sofia.uni.fmi.mjt.frauddetector.rule;

import bg.sofia.uni.fmi.mjt.frauddetector.transaction.Channel;
import bg.sofia.uni.fmi.mjt.frauddetector.transaction.Transaction;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FrequencyRuleTest {
    @Test
    void applicableCorrectly() {
        Rule rule = new FrequencyRule(4, Period.ofDays(3), 0.8);
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
                LocalDateTime.of(2023, 4, 12, 16, 29, 14),
                "San Diego",
                Channel.ATM));
        transactions.add(new Transaction("TX000099",
                "SD202341",
                18.10,
                LocalDateTime.of(2023, 4, 12, 18, 29, 14),
                "San Diego",
                Channel.ATM));
        assertEquals(false, rule.applicable(transactions));
        transactions.add(new Transaction("TX000006",
                "SD20232",
                200,
                LocalDateTime.of(2023, 4, 13, 16, 29, 14),
                "Sofia",
                Channel.BRANCH));
        assertEquals(true, rule.applicable(transactions));
    }
}
