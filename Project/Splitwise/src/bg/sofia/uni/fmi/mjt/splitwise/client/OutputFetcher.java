package bg.sofia.uni.fmi.mjt.splitwise.client;

import java.io.BufferedReader;
import java.io.IOException;

public class OutputFetcher implements Runnable {
    private final BufferedReader reader;

    public OutputFetcher(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public void run() {
        try {
            if (!reader.ready()) {
                return;
            }

            String line;
            while ((line = reader.readLine()) != null && !line.equals("$end$")) {
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
