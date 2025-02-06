package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupExpensesRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalExpensesRepository;

import java.io.PrintWriter;

public class SplitWithGroupCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 3;
    private final Authenticator authenticator;
    private final GroupExpensesRepository expensesRepository;

    private static final int AMOUNT_INDEX = 0;
    private static final int GROUP_NAME_INDEX = 1;
    private static final int REASON_INDEX = 2;

    public SplitWithGroupCommand(Authenticator authenticator, GroupExpensesRepository expensesRepository, String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.expensesRepository = expensesRepository;
    }

    @Override
    public void execute(PrintWriter writer) {
        if (!authenticator.isAuthenticated()) {
            writer.println("You have to be logged in!");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(arguments[AMOUNT_INDEX]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount!", e);
        }

        expensesRepository.addExpense(authenticator.getAuthenticatedUser().username(),
                arguments[GROUP_NAME_INDEX],
                amount,
                arguments[REASON_INDEX]);
        writer.println("Successfully split %s LV with group %s for \"%s\"."
                .formatted(amount, arguments[GROUP_NAME_INDEX], arguments[REASON_INDEX]));
    }

    public static CommandHelp help() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("amount", "the amount a user should pay you", false);
        parameters.addParameter("group-name", "the name of the group with which you split your bill", false);
        parameters.addParameter("reason", "the reason for splitting", false);

        return new CommandHelp("split-group",
                "with this command you can mark that a all users owe you " +
                        "an equal amount of money for a specific reason",
                parameters);
    }
}
