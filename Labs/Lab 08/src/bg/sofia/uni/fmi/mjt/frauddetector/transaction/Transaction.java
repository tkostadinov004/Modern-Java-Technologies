package bg.sofia.uni.fmi.mjt.frauddetector.transaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Transaction(String transactionID,
                          String accountID,
                          double transactionAmount,
                          LocalDateTime transactionDate,
                          String location,
                          Channel channel) {
    private static Channel channelFactory(String channel) {
        return switch (channel) {
            case "ATM" -> Channel.ATM;
            case "Online" -> Channel.ONLINE;
            case "Branch" -> Channel.BRANCH;
            default -> throw new IllegalArgumentException("Illegal channel!");
        };
    }

    public static Transaction of(String line) {
        String[] splittedLine = line.split(",");
        final int lineLength = 6;
        if (splittedLine.length < lineLength) {
            throw new IllegalArgumentException("Invalid line size!");
        }
        final int tIDIndex = 0;
        final int accountIDIndex = 1;
        final int transactionAmountIndex = 2;
        final int transactionDateIndex = 3;
        final int locationIndex = 4;
        final int channelIndex = 5;
        return new Transaction(splittedLine[tIDIndex],
                splittedLine[accountIDIndex],
                Double.parseDouble(splittedLine[transactionAmountIndex]),
                LocalDateTime.parse(splittedLine[transactionDateIndex],
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                splittedLine[locationIndex],
                channelFactory(splittedLine[channelIndex]));
    }
}
