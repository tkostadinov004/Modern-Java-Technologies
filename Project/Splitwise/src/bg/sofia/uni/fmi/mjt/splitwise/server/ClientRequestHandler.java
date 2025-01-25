package bg.sofia.uni.fmi.mjt.splitwise.server;

import bg.sofia.uni.fmi.mjt.splitwise.server.command.factory.CommandFactory;

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

    }
}