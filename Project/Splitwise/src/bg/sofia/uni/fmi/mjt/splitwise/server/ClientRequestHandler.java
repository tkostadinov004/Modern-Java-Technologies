package bg.sofia.uni.fmi.mjt.splitwise.server;

import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.factory.CommandFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientRequestHandler implements Runnable {
    private final Socket socket;
    private final CommandFactory commandFactory;

    public ClientRequestHandler(Socket socket, CommandFactory commandFactory) {
        this.socket = socket;
        this.commandFactory = commandFactory;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            String input;
            while ((input = reader.readLine()) != null) {
                try {
                    Command command = commandFactory.build(input);
                    command.execute(writer);
                } catch (Exception e) {
                    writer.println(e.getMessage());
                }
                writer.println("$end$");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}