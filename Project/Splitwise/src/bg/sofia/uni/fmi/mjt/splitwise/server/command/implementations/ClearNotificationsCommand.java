package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalDebtsRepository;

import java.io.PrintWriter;

public class ClearNotificationsCommand extends Command {
    private static final int ARGUMENTS_NEEDED = 0;
    private Authenticator authenticator;
    private NotificationsRepository notificationsRepository;

    public ClearNotificationsCommand(Authenticator authenticator, NotificationsRepository notificationsRepository, String[] args) {
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

        notificationsRepository.removeAllNotificationsForUser(authenticator.getAuthenticatedUser().username());
        writer.println("Successfully cleared all notifications.");
    }
}
