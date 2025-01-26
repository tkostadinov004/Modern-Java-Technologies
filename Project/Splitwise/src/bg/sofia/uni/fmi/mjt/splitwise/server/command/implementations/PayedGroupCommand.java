package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupDebtsRepository;

import java.io.PrintWriter;

public class PayedGroupCommand extends Command {
    private static final int ARGUMENTS_NEEDED = 4;
    private Authenticator authenticator;
    private GroupDebtsRepository groupDebtsRepository;

    private static final int AMOUNT_INDEX = 0;
    private static final int USERNAME_INDEX = 1;
    private static final int GROUP_NAME_INDEX = 2;
    private static final int REASON_INDEX = 3;

    public PayedGroupCommand(Authenticator authenticator, GroupDebtsRepository groupDebtsRepository, String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.groupDebtsRepository = groupDebtsRepository;
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

        groupDebtsRepository.updateDebt(arguments[USERNAME_INDEX],
                authenticator.getAuthenticatedUser().username(),
                arguments[GROUP_NAME_INDEX],
                amount,
                arguments[REASON_INDEX]);
        writer.println("%s payed you %s LV for \"%s\" in group %s.".formatted(arguments[USERNAME_INDEX], amount, arguments[REASON_INDEX], arguments[GROUP_NAME_INDEX]));
    }
}
