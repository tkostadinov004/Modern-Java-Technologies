package bg.sofia.uni.fmi.mjt.splitwise.server.command;

import java.io.PrintWriter;

public abstract class Command {
    protected String[] arguments;

    public Command(int argumentsNeeded, String[] args) {
        setArguments(argumentsNeeded, args);
    }

    private void setArguments(int argumentsNeeded, String[] args) {
        if (args.length != argumentsNeeded) {
            throw new IllegalArgumentException("Invalid argument count! Expected %s but was %s.".formatted(arguments, args.length));
        }
        this.arguments = args;
    }

    public abstract void execute(PrintWriter writer);
}
