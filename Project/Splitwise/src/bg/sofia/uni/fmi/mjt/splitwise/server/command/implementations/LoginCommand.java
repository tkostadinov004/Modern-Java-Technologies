package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.AlreadyAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;

import java.io.PrintWriter;

public class LoginCommand extends Command {
    private static final int ARGUMENTS_NEEDED = 2;
    private Authenticator authenticator;

    private static final int USERNAME_INDEX = 0;
    private static final int PASSWORD_INDEX = 1;

    public LoginCommand(Authenticator authenticator, String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
    }

    @Override
    public void execute(PrintWriter writer) {
        try {
            authenticator.authenticate(arguments[USERNAME_INDEX], arguments[PASSWORD_INDEX]);
            writer.println("Successfully logged in!");
        } catch (AlreadyAuthenticatedException e) {
            writer.println(e.getMessage());
        }
    }
}
