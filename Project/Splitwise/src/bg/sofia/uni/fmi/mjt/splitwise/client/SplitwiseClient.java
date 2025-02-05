package bg.sofia.uni.fmi.mjt.splitwise.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SplitwiseClient {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             ScheduledExecutorService executor =
                     Executors.newScheduledThreadPool(1, Thread.ofVirtual().factory())) {

            executor.scheduleAtFixedRate(new OutputFetcher(reader),
                    0, 1, TimeUnit.SECONDS);

            Scanner sc = new Scanner(System.in);
            while (true) {
                String message = sc.nextLine();
                if (message.equals("exit")) {
                    break;
                }

                writer.println(message);
            }
        } catch (IOException e) {
            System.out.println("Unexpected connection error!");
        }
    }
}
