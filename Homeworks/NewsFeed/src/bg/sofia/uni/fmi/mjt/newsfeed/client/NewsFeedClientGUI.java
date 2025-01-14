package bg.sofia.uni.fmi.mjt.newsfeed.client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class NewsFeedClientGUI {
    private BufferedWriter writer;

    public NewsFeedClientGUI(OutputStream outputStream) {
        this.writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    public void printIntroduction() throws IOException {
        writer.write("");
    }

    private void printFetchHelp() throws IOException {
        writer.write("fetch [country] [category] [keyword] [pageSize]");
        writer.write("fetch [sources] [keyword] [pageSize]");
    }

    public void printCommands() throws IOException {
        writer.write("connect <ip-address> <port> - connects you to the server with the given IP and port\n");
        writer.write("disconnect - disconnects you from the server\n");
        writer.write("help - shows you how to work with the news feed application\n\n");
        printFetchHelp();
    }

    public void shutdown() throws IOException {

    }
}
