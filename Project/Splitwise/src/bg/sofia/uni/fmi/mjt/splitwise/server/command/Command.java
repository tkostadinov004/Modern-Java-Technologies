package bg.sofia.uni.fmi.mjt.splitwise.server.command;

import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;

import java.io.PrintWriter;

public abstract class Command {
    protected String[] arguments;

    public Command(int argumentsNeeded, String[] args) {
        setArguments(argumentsNeeded, args);
    }

    protected abstract void setArguments(int argumentsNeeded, String[] args);

    public abstract void execute(PrintWriter writer);
}
