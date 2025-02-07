package bg.sofia.uni.fmi.mjt.splitwise.server.command;

import bg.sofia.uni.fmi.mjt.splitwise.server.command.exception.CommandArgumentsCountException;

public abstract class StandardCommand extends Command {
    public StandardCommand(int argumentsNeeded, String[] args) {
        super(argumentsNeeded, args);
    }

    @Override
    protected void setArguments(int argumentsNeeded, String[] args) {
        if (args.length != argumentsNeeded) {
            throw new CommandArgumentsCountException("Invalid argument count! Expected %s but was %s."
                    .formatted(argumentsNeeded, args.length));
        }
        this.arguments = args;
    }
}
