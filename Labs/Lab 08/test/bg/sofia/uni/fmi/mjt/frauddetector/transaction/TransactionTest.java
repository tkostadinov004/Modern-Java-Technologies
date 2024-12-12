package bg.sofia.uni.fmi.mjt.frauddetector.transaction;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TransactionTest {
    @Test
    public void factoryThrowsOnInvalidString() {
        assertThrows(IllegalArgumentException.class,
                () -> Transaction.of(""),
                "Transaction.of() should throw if string is invalid!");
        assertThrows(IllegalArgumentException.class,
                () -> Transaction.of("a,b"),
                "Transaction.of() should throw if string is invalid!");
    }
    @Test
    public void factoryThrowsOnInvalidChannel() {
        assertThrows(IllegalArgumentException.class,
                () -> Transaction.of("TX000001,AC00128,14.09,2023-04-11 16:29:14,San Diego,ASD"),
                "Transaction.of() should throw if channel is invalid!");
    }
    @Test
    public void factoryCreatesCorrectlyWithValidInput() {
        String line = "TX000001,AC00128,14.09,2023-04-11 16:29:14,San Diego,ATM";
        Transaction expected = new Transaction("TX000001",
                "AC00128",
                14.09,
                LocalDateTime.of(2023, 4, 11, 16, 29, 14),
                "San Diego",
                Channel.ATM);
        Transaction actual = Transaction.of(line);
        assertEquals(expected.transactionID(), actual.transactionID());
        assertEquals(expected.accountID(), actual.accountID());
        assertEquals(expected.transactionAmount(), actual.transactionAmount());
        assertEquals(expected.transactionDate(), actual.transactionDate());
        assertEquals(expected.location(), actual.location());
        assertEquals(expected.channel(), actual.channel());
    }
}
