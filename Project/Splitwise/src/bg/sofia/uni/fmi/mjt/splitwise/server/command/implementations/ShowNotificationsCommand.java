package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;

import java.io.PrintWriter;
import java.util.Set;

public class ShowNotificationsCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 0;
    private final Authenticator authenticator;
    private final NotificationsRepository notificationsRepository;

    public ShowNotificationsCommand(Authenticator authenticator,
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
            Set<Notification> notifications = notificationsRepository
                    .getNotificationsForUser(authenticator.getAuthenticatedUser().username());

            if (!notifications.isEmpty()) {
                notifications
                        .forEach(notification -> writer.println("* %s".formatted(notification)));
            } else {
                writer.println("<no notifications>");
            }
            return true;
        } catch (RuntimeException e) {
            writer.println(e.getMessage());
            return false;
        }
    }

    public static CommandHelp help() {
        return new CommandHelp("show-notifications",
                "prints all notifications you have received",
                new ParameterContainer());
    }
}
