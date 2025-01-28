package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;

import java.io.PrintWriter;
import java.util.Set;

public class ShowNotificationsCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 0;
    private Authenticator authenticator;
    private NotificationsRepository notificationsRepository;

    public ShowNotificationsCommand(Authenticator authenticator, NotificationsRepository notificationsRepository, String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.notificationsRepository = notificationsRepository;
    }

    @Override
    public void execute(PrintWriter writer) {
        if (!authenticator.isAuthenticated()) {
            writer.println("You have to be logged in!");
            return;
        }

        Set<Notification> notifications = notificationsRepository
                .getNotificationForUser(authenticator.getAuthenticatedUser().username());

        if (!notifications.isEmpty()) {
            notifications
                    .forEach(notification -> writer.println("* %s".formatted(notification)));
        } else {
            writer.println("<no notifications>");
        }
    }

    public static CommandHelp help() {
        return new CommandHelp("show-notifications",
                "prints all notifications you have received",
                new ParameterContainer());
    }
}
