package bg.sofia.uni.fmi.mjt.splitwise.server;

import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.factory.CommandFactory;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.factory.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Logger;

public class ClientRequestHandler implements Runnable {
    private final Logger logger;
    private final Socket socket;
    private final CommandFactory commandFactory;

    public ClientRequestHandler(DependencyContainer dependencyContainer, Socket socket, CommandFactory commandFactory) {
        this.logger = dependencyContainer.get(Logger.class);
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
                    Command command = commandFactory.build(input, new CommandParser());
                    command.execute(writer);
                } catch (Exception e) {
                    logger.severe(e.getMessage());
                    logger.severe(Arrays.toString(e.getStackTrace()));
                    writer.println(e.getMessage());
                }
                writer.println("$end$");
            }
        } catch (IOException e) {
            logger.severe(e.getMessage());
            logger.severe(Arrays.toString(e.getStackTrace()));
        }
    }
}