package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.FriendGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Debt;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalDebtsRepository;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

public class StatusCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 0;

    private Authenticator authenticator;
    private PersonalDebtsRepository personalDebtsRepository;
    private GroupDebtsRepository groupDebtsRepository;

    public StatusCommand(Authenticator authenticator, PersonalDebtsRepository personalDebtsRepository, GroupDebtsRepository groupDebtsRepository, String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.personalDebtsRepository = personalDebtsRepository;
        this.groupDebtsRepository = groupDebtsRepository;
    }

    private void printDebt(Debt debt, PrintWriter writer) {
        writer.print("* ");
        if (debt.debtor().equals(authenticator.getAuthenticatedUser())) {
            writer.println("%s: You owe %s LV. [%s]".formatted(debt.recipient(), debt.amount(), debt.reason()));
        } else {
            writer.println("%s: Owes you %s LV. [%s]".formatted(debt.debtor(), debt.amount(), debt.reason()));
        }
    }

    private void printDebtEntry(Map.Entry<FriendGroup, Set<Debt>> debtEntry, PrintWriter writer) {
        writer.println("%s: ".formatted(debtEntry.getKey().name()));
        if (debtEntry.getValue().isEmpty()) {
            writer.println("<no debts>");
        } else {
            debtEntry.getValue().forEach(debt -> printDebt(debt, writer));
        }
    }

    @Override
    public void execute(PrintWriter writer) {
        if (!authenticator.isAuthenticated()) {
            writer.println("You have to be logged in!");
            return;
        }

        writer.println("Friends:");
        Set<Debt> personalDebts = personalDebtsRepository.getDebtsOf(authenticator.getAuthenticatedUser().username());
        if (!personalDebts.isEmpty()) {
            personalDebts.forEach(debt -> printDebt(debt, writer));
        } else {
            writer.println("<no debts>");
        }

        writer.println("Groups:");
        Map<FriendGroup, Set<Debt>> groupDebts = groupDebtsRepository.getDebtsOf(authenticator.getAuthenticatedUser().username());
        if (!groupDebts.isEmpty()) {
            groupDebts.entrySet().forEach(debtEntry -> printDebtEntry(debtEntry, writer));
        } else {
            writer.println("<no debts>");
        }
    }

    public static CommandHelp help() {
        return new CommandHelp("get-status",
                "prints the people you owe money to and the people who owe money to you",
                new ParameterContainer());
    }
}
