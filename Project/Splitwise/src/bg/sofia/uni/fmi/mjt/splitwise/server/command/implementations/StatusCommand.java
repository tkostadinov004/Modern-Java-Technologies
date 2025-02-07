package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.GroupDebt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.PersonalDebt;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalDebtsRepository;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

public class StatusCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 0;
    private final Authenticator authenticator;
    private final PersonalDebtsRepository personalDebtsRepository;
    private final GroupDebtsRepository groupDebtsRepository;

    public StatusCommand(Authenticator authenticator,
                         PersonalDebtsRepository personalDebtsRepository,
                         GroupDebtsRepository groupDebtsRepository,
                         String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.personalDebtsRepository = personalDebtsRepository;
        this.groupDebtsRepository = groupDebtsRepository;
    }

    private void printDebt(User debtor, User recipient, double amount, String reason, PrintWriter writer) {
        writer.print("* ");
        if (debtor.equals(authenticator.getAuthenticatedUser())) {
            writer.println("%s: You owe %s LV. [%s]".formatted(recipient, amount, reason));
        } else {
            writer.println("%s: Owes you %s LV. [%s]".formatted(debtor, amount, reason));
        }
    }

    private void printGroupDebt(Map.Entry<FriendGroup, Set<GroupDebt>> debtEntry, PrintWriter writer) {
        writer.println("%s: ".formatted(debtEntry.getKey().name()));
        if (debtEntry.getValue().isEmpty()) {
            writer.println("<no debts>");
        } else {
            debtEntry.getValue().forEach(debt -> printDebt(debt.debtor(),
                    debt.recipient(), debt.amount(), debt.reason(), writer));
        }
    }

    @Override
    public boolean execute(PrintWriter writer) {
        if (!authenticator.isAuthenticated()) {
            writer.println("You have to be logged in!");
            return false;
        }
        try {
            writer.println("Friends:");
            Set<PersonalDebt> personalDebts =
                    personalDebtsRepository.getDebtsOf(authenticator.getAuthenticatedUser().username());
            if (!personalDebts.isEmpty()) {
                personalDebts.forEach(debt -> printDebt(debt.debtor(),
                        debt.recipient(), debt.amount(), debt.reason(), writer));
            } else {
                writer.println("<no debts>");
            }

            writer.println("Groups:");
            Map<FriendGroup, Set<GroupDebt>> groupDebts =
                    groupDebtsRepository.getDebtsOf(authenticator.getAuthenticatedUser().username());
            if (!groupDebts.isEmpty()) {
                groupDebts.entrySet().forEach(debtEntry -> printGroupDebt(debtEntry, writer));
            } else {
                writer.println("<no debts>");
            }
            return true;
        } catch (RuntimeException e) {
            writer.println(e.getMessage());
            return false;
        }
    }

    public static CommandHelp help() {
        return new CommandHelp("get-status",
                "prints the people you owe money to and the people who owe money to you",
                new ParameterContainer());
    }
}
