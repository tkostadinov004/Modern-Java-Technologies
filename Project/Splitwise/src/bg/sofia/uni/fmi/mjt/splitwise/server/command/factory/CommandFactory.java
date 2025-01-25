package bg.sofia.uni.fmi.mjt.splitwise.server.command.factory;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.factory.parser.CommandParser;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.factory.parser.ParsedCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ExpensesRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserFriendsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;

public class CommandFactory implements Factory<Command> {
    private final Authenticator authenticator;
    private final ExpensesRepository expensesRepository;
    private final FriendGroupRepository friendGroupRepository;
    private final GroupDebtsRepository groupDebtsRepository;
    private final NotificationsRepository notificationsRepository;
    private final PersonalDebtsRepository personalDebtsRepository;
    private final UserFriendsRepository userFriendsRepository;
    private final UserRepository userRepository;

    public CommandFactory(Authenticator authenticator,
                          ExpensesRepository expensesRepository,
                          FriendGroupRepository friendGroupRepository,
                          GroupDebtsRepository groupDebtsRepository,
                          NotificationsRepository notificationsRepository,
                          PersonalDebtsRepository personalDebtsRepository,
                          UserFriendsRepository userFriendsRepository,
                          UserRepository userRepository) {
        this.authenticator = authenticator;
        this.expensesRepository = expensesRepository;
        this.friendGroupRepository = friendGroupRepository;
        this.groupDebtsRepository = groupDebtsRepository;
        this.notificationsRepository = notificationsRepository;
        this.personalDebtsRepository = personalDebtsRepository;
        this.userFriendsRepository = userFriendsRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Command build(String input) {
        ParsedCommand command = new CommandParser().parse(input);
        if (command == null) {
            throw new IllegalArgumentException("Input cannot be null, blank, or empty!");
        }

        return switch (command.name()) {
            case "login" -> new LoginCommand(authenticator, command.args());
            case "register" -> new RegisterCommand(authenticator, userRepository, command.args());
            case "help" -> new HelpCommand(command.args());
            case "add-friend" -> new AddFriendCommand(authenticator, userFriendsRepository, command.args());
            case "create-group" -> new CreateGroupCommand(authenticator, friendGroupRepository, command.args());
            case "split", "split-group" -> new SplitCommand(authenticator, expensesRepository, command.args());
            case "get-status" -> new StatusCommand(authenticator, personalDebtsRepository, groupDebtsRepository, command.args());
            case "payed" -> new PayedCommand(authenticator, personalDebtsRepository, command.args());
            case "payed-group" -> new PayedCommand(authenticator, groupDebtsRepository, command.args());
            case "clear-notifications" -> new ClearNotificationCommand(authenticator, notificationsRepository, command.args());
            case "chat-with" -> new ChatCommand(authenticator, command.args());
            case "export-recent-expenses" -> new ExportRecentExpensesCommand(authenticator, expensesRepository, command.args());
        };
    }
}
