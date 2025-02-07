package bg.sofia.uni.fmi.mjt.splitwise.server.command;

import bg.sofia.uni.fmi.mjt.splitwise.server.command.exception.CommandArgumentsCountException;

public abstract class VariableArgumentsCommand extends Command {
    public VariableArgumentsCommand(int argumentsNeeded, String[] args) {
        super(argumentsNeeded, args);
    }

    @Override
    protected void setArguments(int argumentsNeeded, String[] args) {
        if (args.length < argumentsNeeded) {
            throw new CommandArgumentsCountException("Insufficient argument amount! Expected at least %s but was %s."
                    .formatted(argumentsNeeded, args.length));
        }
        this.arguments = args;
    }
}
