package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.StandardCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;

import java.io.PrintWriter;

public class RegisterCommand extends StandardCommand {
    private static final int ARGUMENTS_NEEDED = 4;
    private final Authenticator authenticator;
    private final UserRepository userRepository;

    private static final int USERNAME_INDEX = 0;
    private static final int PASSWORD_INDEX = 1;
    private static final int FIRST_NAME_INDEX = 2;
    private static final int LAST_NAME_INDEX = 3;

    public RegisterCommand(Authenticator authenticator, UserRepository userRepository, String[] args) {
        super(ARGUMENTS_NEEDED, args);
        this.authenticator = authenticator;
        this.userRepository = userRepository;
    }

    @Override
    public boolean execute(PrintWriter writer) {
        if (authenticator.isAuthenticated()) {
            writer.println("You cannot register if you are already logged in. Log out first!");
            return false;
        }

        try {
            userRepository.registerUser(arguments[USERNAME_INDEX],
                    arguments[PASSWORD_INDEX],
                    arguments[FIRST_NAME_INDEX],
                    arguments[LAST_NAME_INDEX]);
            writer.println("Successfully registered!");
            return true;
        } catch (RuntimeException e) {
            writer.println(e.getMessage());
            return false;
        }
    }

    public static CommandHelp help() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("username", "your username", false);
        parameters.addParameter("password", "your password", false);
        parameters.addParameter("first-name", "your first name", false);
        parameters.addParameter("last-name", "your last name", false);

        return new CommandHelp("register",
                "registers you in the system",
                parameters);
    }
}
