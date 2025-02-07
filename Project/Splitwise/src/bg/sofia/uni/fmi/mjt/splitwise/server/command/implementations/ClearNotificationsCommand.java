package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;

import java.io.PrintWriter;

public class ClearNotificationsCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 0;
    private final Authenticator authenticator;
    private final NotificationsRepository notificationsRepository;

    public ClearNotificationsCommand(Authenticator authenticator,
                                     NotificationsRepository notificationsRepository,
                                     String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.notificationsRepository = notificationsRepository;
    }

    @Override
    public boolean execute(PrintWriter writer) {
        if (!authenticator.isAuthenticated()) {
            writer.println("You have to be logged in!");
            return false;
        }

        try {
            notificationsRepository.removeAllNotificationsForUser(authenticator.getAuthenticatedUser().username());
            writer.println("Successfully cleared all notifications.");
            return true;
        } catch (RuntimeException e) {
            writer.println(e.getMessage());
            return false;
        }
    }

    public static CommandHelp help() {
        return new CommandHelp("clear-notifications",
                "clears the notifications that you have, meaning that they won't be shown again when you log in",
                new ParameterContainer());
    }
}
