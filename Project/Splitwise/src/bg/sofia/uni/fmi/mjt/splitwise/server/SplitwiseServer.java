package bg.sofia.uni.fmi.mjt.splitwise.server;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.DefaultAuthenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.token.ChatToken;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.token.DefaultChatToken;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.factory.CommandFactory;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.ExpensesCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.FriendGroupsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.GroupDebtsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.NotificationsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.PersonalDebtsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.UserCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.UserFriendsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ChatRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ExpensesRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.FriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.GroupDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.NotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.PersonalDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserFriendsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.DefaultChatRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.DefaultExpensesRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.DefaultFriendGroupRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.DefaultGroupDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.DefaultNotificationsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.DefaultPersonalDebtsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.DefaultUserFriendsRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.DefaultUserRepository;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class SplitwiseServer {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public void start() {
        while (true) {
            try (ServerSocket serverSocket = new ServerSocket()) {
                serverSocket.bind(new InetSocketAddress(HOST, PORT));
                File users = new File("users.csv");
                if (!users.exists()) {
                    users.createNewFile();
                }
                File userFriends = new File("user-friends.csv");
                if (!userFriends.exists()) {
                    userFriends.createNewFile();
                }
                File notifications = new File("notifications.csv");
                if (!notifications.exists()) {
                    notifications.createNewFile();
                }
                File personalDebts = new File("personal-debts.csv");
                if (!personalDebts.exists()) {
                    personalDebts.createNewFile();
                }
                File friendGroups = new File("friend-groups.csv");
                if (!friendGroups.exists()) {
                    friendGroups.createNewFile();
                }
                File groupDebts = new File("group-debts.csv");
                if (!groupDebts.exists()) {
                    groupDebts.createNewFile();
                }
                File expenses = new File("expenses.csv");
                if (!expenses.exists()) {
                    expenses.createNewFile();
                }


                UserRepository userRepository = new DefaultUserRepository(new UserCsvProcessor(new CSVReader(new FileReader("users.csv")), "users.csv"));
                UserFriendsRepository userFriendsRepository = new DefaultUserFriendsRepository(new UserFriendsCsvProcessor(userRepository, new CSVReader(new FileReader("user-friends.csv")), "user-friends.csv"), userRepository);
                NotificationsRepository notificationsRepository = new DefaultNotificationsRepository(new NotificationsCsvProcessor(userRepository, new CSVReader(new FileReader("notifications.csv")), "notifications.csv"), userRepository);
                PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(new PersonalDebtsCsvProcessor(userRepository, new CSVReader(new FileReader("personal-debts.csv")), "personal-debts.csv"), userRepository, notificationsRepository);
                FriendGroupRepository friendGroupRepository = new DefaultFriendGroupRepository(new FriendGroupsCsvProcessor(userRepository, new CSVReader(new FileReader("friend-groups.csv")), "friend-groups.csv"), userRepository, userFriendsRepository);
                GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(new GroupDebtsCsvProcessor(userRepository, friendGroupRepository, new CSVReader(new FileReader("group-debts.csv")), "group-debts.csv"), userRepository, friendGroupRepository, notificationsRepository);
                ExpensesRepository expensesRepository = new DefaultExpensesRepository(new ExpensesCsvProcessor(userRepository, new CSVReader(new FileReader("expenses.csv")), "expenses.csv"), userRepository, friendGroupRepository, personalDebtsRepository, groupDebtsRepository, notificationsRepository);
                ChatRepository chatRepository = new DefaultChatRepository(new InetSocketAddress(serverSocket.getInetAddress(), PORT), userRepository);

                while (true) {
                    Socket client = serverSocket.accept();
                    Authenticator authenticator = new DefaultAuthenticator(userRepository, client);
                    ChatToken chatToken = new DefaultChatToken(authenticator, userRepository, chatRepository);
                    CommandFactory commandFactory = new CommandFactory(authenticator, chatToken, chatRepository, expensesRepository, friendGroupRepository, groupDebtsRepository, notificationsRepository, personalDebtsRepository, userFriendsRepository, userRepository);
                    new Thread(() -> new ClientRequestHandler(client, commandFactory).run()).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class Program {
    public static void main(String[] args) {
        new SplitwiseServer().start();
    }
}