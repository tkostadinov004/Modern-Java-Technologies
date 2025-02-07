package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.NotAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;

import java.io.PrintWriter;

public class LogoutCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 0;
    private final Authenticator authenticator;

    public LogoutCommand(Authenticator authenticator, String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
    }

    @Override
    public boolean execute(PrintWriter writer) {
        try {
            authenticator.logout();
            writer.println("Successfully logged out!");
            return true;
        } catch (NotAuthenticatedException e) {
            writer.println(e.getMessage());
            return false;
        }
    }

    public static CommandHelp help() {
        return new CommandHelp("logout",
                "logs you out of the system",
                new ParameterContainer());
    }
}
