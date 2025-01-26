package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ExpensesRepository;

import java.io.PrintWriter;

public class SplitWithGroupCommand extends Command {
    private static final int ARGUMENTS_NEEDED = 3;
    private Authenticator authenticator;
    private ExpensesRepository expensesRepository;

    private static final int AMOUNT_INDEX = 0;
    private static final int GROUP_NAME_INDEX = 1;
    private static final int REASON_INDEX = 2;

    public SplitWithGroupCommand(Authenticator authenticator, ExpensesRepository expensesRepository, String[] args) {
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

        expensesRepository.addGroupExpense(authenticator.getAuthenticatedUser().username(),
                arguments[GROUP_NAME_INDEX],
                amount,
                arguments[REASON_INDEX]);
        writer.println("Successfully split %s LV with group %s for \"%s\".".formatted(amount, arguments[GROUP_NAME_INDEX], arguments[REASON_INDEX]));
    }
}
