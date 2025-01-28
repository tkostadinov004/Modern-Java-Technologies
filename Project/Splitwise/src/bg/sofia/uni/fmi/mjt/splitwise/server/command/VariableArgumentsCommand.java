package bg.sofia.uni.fmi.mjt.splitwise.server.command;

public abstract class VariableArgumentsCommand extends Command {
    public VariableArgumentsCommand(int argumentsNeeded, String[] args) {
        super(argumentsNeeded, args);
    }

    @Override
    protected void setArguments(int argumentsNeeded, String[] args) {
        if (args.length < argumentsNeeded) {
            throw new IllegalArgumentException("Insufficient argument amount! Expected at least %s but was %s."
                    .formatted(argumentsNeeded, args.length));
        }
        this.arguments = args;
    }
}
