package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.AlreadyAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LoginCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 2;
    private final Authenticator authenticator;
    private final NotificationsRepository notificationsRepository;

    private static final int USERNAME_INDEX = 0;
    private static final int PASSWORD_INDEX = 1;

    public LoginCommand(Authenticator authenticator, NotificationsRepository notificationsRepository, String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.notificationsRepository = notificationsRepository;
    }

    private void printNotifications(String username, PrintWriter writer) {
        Map<NotificationType, List<Notification>> notifications = notificationsRepository
                .getNotificationsForUser(username)
                        .stream().collect(Collectors.groupingBy(n -> n.type()));
        if (notifications.isEmpty()) {
            return;
        }

        writer.println("*** Notifications ***");
        notifications
                .entrySet()
                .forEach(group -> {
                    writer.println(group.getKey() + ":");
                    group.getValue().forEach(notification -> writer.println(notification));
                    writer.println(System.lineSeparator());
                });
    }

    @Override
    public boolean execute(PrintWriter writer) {
        try {
            authenticator.authenticate(arguments[USERNAME_INDEX], arguments[PASSWORD_INDEX]);
            writer.println("Successfully logged in!");
            printNotifications(arguments[USERNAME_INDEX], writer);
            return true;
        } catch (AlreadyAuthenticatedException | IllegalArgumentException e) {
            writer.println(e.getMessage());
            return false;
        }
    }

    public static CommandHelp help() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("username", "your username", false);
        parameters.addParameter("password", "your password", false);

        return new CommandHelp("login",
                "logs you in the system",
                parameters);
    }
}
