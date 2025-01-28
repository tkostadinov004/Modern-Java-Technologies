package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ExpensesRepository;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ExportRecentExpensesCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 2;
    private Authenticator authenticator;
    private ExpensesRepository expensesRepository;

    public ExportRecentExpensesCommand(Authenticator authenticator, ExpensesRepository expensesRepository, String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.expensesRepository = expensesRepository;
    }

    private static final int COUNT_INDEX = 0;
    private static final int FILENAME_INDEX = 1;

    @Override
    public void execute(PrintWriter writer) {
        if (!authenticator.isAuthenticated()) {
            writer.println("You have to be logged in!");
            return;
        }

        int count;
        try {
            count = Integer.parseInt(arguments[COUNT_INDEX]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount!", e);
        }

        try {
            expensesRepository.exportRecent(authenticator.getAuthenticatedUser().username(),
                    count,
                    new FileWriter(arguments[FILENAME_INDEX]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static CommandHelp help() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("count", "the amount of expenses you want exported", false);
        parameters.addParameter("filename", "the name of the file you would want the expenses exported to", false);

        return new CommandHelp("export-recent-expenses",
                "exports the most recent expenses you have made in a specified CSV file",
                parameters);
    }
}
