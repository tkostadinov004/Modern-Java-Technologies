package bg.sofia.uni.fmi.mjt.splitwise.server.command.implementations;

import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.VariableArgumentsCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.CommandHelp;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.help.ParameterContainer;
import org.apache.commons.logging.Log;

import java.io.PrintWriter;

public class HelpCommand extends VariableArgumentsCommand {
    private static final int ARGUMENTS_NEEDED = 0;

    public HelpCommand(String[] args) {
        super(ARGUMENTS_NEEDED, args);
    }

    private CommandHelp getSpecificCommandInfo(String commandName, PrintWriter writer) {
        return switch (commandName) {
            case "login" -> LoginCommand.help();
            case "register" -> RegisterCommand.help();
            case "logout" -> LogoutCommand.help();
            case "help" -> HelpCommand.help();
            case "add-friend" -> AddFriendCommand.help();
            case "create-group" -> CreateGroupCommand.help();
            case "split" -> SplitCommand.help();
            case "split-group" -> SplitWithGroupCommand.help();
            case "get-status" -> StatusCommand.help();
            case "payed" -> PayedCommand.help();
            case "payed-group" -> PayedGroupCommand.help();
            case "show-notifications" -> ShowNotificationsCommand.help();
            case "clear-notifications" -> ClearNotificationsCommand.help();
            case "create-chat" -> CreateChatCommand.help();
            case "join-chat" -> JoinChatCommand.help();
            case "send-message-chat" -> SendMessageInChatCommand.help();
            case "exit-chat" -> ExitChatCommand.help();
            case "export-recent-expenses" -> ExportRecentExpensesCommand.help();
            default -> throw new IllegalArgumentException("Invalid command!");
        };
    }

    void printCommandList(PrintWriter writer) {
        writer.println(LoginCommand.help());
        writer.println(RegisterCommand.help());
        writer.println(LogoutCommand.help());
        writer.println(HelpCommand.help());
        writer.println(AddFriendCommand.help());
        writer.println(CreateGroupCommand.help());
        writer.println(SplitCommand.help());
        writer.println(SplitWithGroupCommand.help());
        writer.println(StatusCommand.help());
        writer.println(PayedCommand.help());
        writer.println(PayedGroupCommand.help());
        writer.println(ShowNotificationsCommand.help());
        writer.println(ClearNotificationsCommand.help());
        writer.println(CreateChatCommand.help());
        writer.println(JoinChatCommand.help());
        writer.println(SendMessageInChatCommand.help());
        writer.println(ExitChatCommand.help());
        writer.println(ExportRecentExpensesCommand.help());
    }

    @Override
    public void execute(PrintWriter writer) {
        if (arguments.length > 0) {
            writer.println(getSpecificCommandInfo(arguments[0], writer));
        } else {
            printCommandList(writer);
        }
    }

    public static CommandHelp help() {
        ParameterContainer parameters = new ParameterContainer();
        parameters.addParameter("command-name", "the name of the command you want to know more about", true);

        return new CommandHelp("help",
                "prints a list of all commands, their descriptions, and parameters, or prints the details of a specific command if it's included",
                parameters);
    }
}
