package bg.sofia.uni.fmi.mjt.splitwise.client;

import bg.sofia.uni.fmi.mjt.splitwise.server.chat.ChatException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SplitwiseClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println(socket.getInetAddress() + " "  + socket.getLocalPort());

            var executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(() -> {
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
            }, 0, 2, TimeUnit.SECONDS);

            Scanner sc = new Scanner(System.in);
            while (true) {
                String message = sc.nextLine();
                if (message.equals("exit")) {
                    break;
                }

                writer.println(message);

                String line;
                while ((line = reader.readLine()) != null && !line.equals("$end$")) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
