package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupExpensesRepository;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ExportRecentGroupExpensesCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 2;
    private final Authenticator authenticator;
    private final GroupExpensesRepository expensesRepository;

    public ExportRecentGroupExpensesCommand(Authenticator authenticator,
                                            GroupExpensesRepository expensesRepository,
                                            String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.expensesRepository = expensesRepository;
    }

    private static final int COUNT_INDEX = 0;
    private static final int FILENAME_INDEX = 1;

    @Override
    public boolean execute(PrintWriter writer) {
        if (!authenticator.isAuthenticated()) {
            writer.println("You have to be logged in!");
            return false;
        }

        int count;
        try {
            count = Integer.parseInt(arguments[COUNT_INDEX]);
        } catch (NumberFormatException e) {
            writer.println("Invalid amount!");
            return false;
        }

        try {
            expensesRepository.exportRecent(authenticator.getAuthenticatedUser().username(),
                    count,
                    new BufferedWriter(new FileWriter(arguments[FILENAME_INDEX])));
            writer.println("Successfully exported expenses to file %s".formatted(arguments[FILENAME_INDEX]));
            return true;
        } catch (IOException | RuntimeException e) {
            writer.println(e.getMessage());
            return false;
        }
    }

    public static CommandHelp help() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("count", "the amount of expenses you want exported", false);
        parameters.addParameter("filename", "the name of the file you would want the expenses exported to", false);

        return new CommandHelp("export-recent-group-expenses",
                "exports the most recent expenses you have made in your groups in a specified CSV file",
                parameters);
    }
}