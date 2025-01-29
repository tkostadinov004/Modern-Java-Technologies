package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.AlreadyAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.NotAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.NotificationType;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogoutCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 0;
    private Authenticator authenticator;

    public LogoutCommand(Authenticator authenticator, String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
    }

    @Override
    public void execute(PrintWriter writer) {
        try {
            authenticator.logout();
            writer.println("Successfully logged out!");
        } catch (NotAuthenticatedException e) {
            writer.println(e.getMessage());
        }
    }

    public static CommandHelp help() {
        return new CommandHelp("logout",
                "logs you out of the system",
                new ParameterContainer());
    }
}
