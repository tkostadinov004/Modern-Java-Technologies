package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ExpensesRepository;

import java.io.PrintWriter;

public class SplitCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 3;
    private final Authenticator authenticator;
    private final ExpensesRepository expensesRepository;

    private static final int AMOUNT_INDEX = 0;
    private static final int USERNAME_INDEX = 1;
    private static final int REASON_INDEX = 2;

    public SplitCommand(Authenticator authenticator, ExpensesRepository expensesRepository, String[] args) {
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

        expensesRepository.addPersonalBaseExpense(authenticator.getAuthenticatedUser().username(),
                arguments[USERNAME_INDEX],
                amount,
                arguments[REASON_INDEX]);
        writer.println("Successfully split %s LV with %s for \"%s\"."
                .formatted(amount, arguments[USERNAME_INDEX], arguments[REASON_INDEX]));
    }

    public static CommandHelp help() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("amount", "the amount a user should pay you", false);
        parameters.addParameter("username", "the username of the user who should pay you", false);
        parameters.addParameter("reason", "the reason for payment", false);

        return new CommandHelp("split",
                "with this command you can mark that a user owes you a given amount of money for a specific reason",
                parameters);
    }
}
