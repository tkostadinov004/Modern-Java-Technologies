package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupDebtsRepository;

import java.io.PrintWriter;

public class PayedGroupCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 4;
    private final Authenticator authenticator;
    private final GroupDebtsRepository groupDebtsRepository;

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
    public boolean execute(PrintWriter writer) {
        if (!authenticator.isAuthenticated()) {
            writer.println("You have to be logged in!");
            return false;
        }

        double amount;
        try {
            amount = Double.parseDouble(arguments[AMOUNT_INDEX]);
        } catch (NumberFormatException e) {
            writer.println("Invalid amount!");
            return false;
        }

        try {
            groupDebtsRepository.lowerDebtBurden(arguments[USERNAME_INDEX],
                    authenticator.getAuthenticatedUser().username(),
                    arguments[GROUP_NAME_INDEX],
                    amount,
                    arguments[REASON_INDEX]);
            writer.println("%s payed you %s LV for \"%s\" in group %s."
                    .formatted(arguments[USERNAME_INDEX], amount,
                            arguments[REASON_INDEX], arguments[GROUP_NAME_INDEX]));
            return true;
        } catch (RuntimeException e) {
            writer.println(e.getMessage());
            return false;
        }
    }

    public static CommandHelp help() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("amount", "the amount a user paid you", false);
        parameters.addParameter("username", "the username of the user who paid you", false);
        parameters.addParameter("group-name", "the name of the group in which the debt is active", false);
        parameters.addParameter("reason", "the reason for payment", false);

        return new CommandHelp("payed-group",
                "with this command you can mark that a user paid you a given amount for a loan " +
                        "they have to pay to you in a specific group",
                parameters);
    }
}
