package bg.sofia.uni.fmi.mjt.splitwise.server;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.DefaultAuthenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.token.ChatToken;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.token.DefaultChatToken;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.factory.CommandFactory;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.DataFilePaths;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.ExpensesCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.FriendGroupsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.GroupDebtsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.NotificationsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.PersonalDebtsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.UserCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.UserFriendsCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class SplitwiseServer {
    private static final String HOST = "localhost";
    private static final String LOGS_DIRECTORY = "logs";
    private static final int PORT = 12345;
    private static final int MAX_CLIENTS = 1024;
    private final DependencyContainer dependencyContainer;
    private final ExecutorService executorService;

    public SplitwiseServer() {
        dependencyContainer = new DependencyContainer();
        executorService = Executors.newFixedThreadPool(MAX_CLIENTS);
    }

    private FileHandler getLoggerHandler() {
        try {
            File dir = new File(LOGS_DIRECTORY);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            SimpleDateFormat format = new SimpleDateFormat("M-d_HHmmss");
            return new FileHandler("logs/log"
                    + format.format(Calendar.getInstance().getTime()) + ".log");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleFiles() throws IOException {
        File dir = new File(DataFilePaths.DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        new File(DataFilePaths.USERS_PATH).createNewFile();
        new File(DataFilePaths.USERS_FRIENDS_PATH).createNewFile();
        new File(DataFilePaths.NOTIFICATIONS_PATH).createNewFile();
        new File(DataFilePaths.PERSONAL_DEBTS_PATH).createNewFile();
        new File(DataFilePaths.FRIEND_GROUPS_PATH).createNewFile();
        new File(DataFilePaths.GROUP_DEBTS_PATH).createNewFile();
        new File(DataFilePaths.EXPENSES_PATH).createNewFile();
    }

    private void registerCSVProcessors() throws FileNotFoundException {
        UserCsvProcessor userCsvProcessor =
                new UserCsvProcessor(new CSVReader(new FileReader(DataFilePaths.USERS_PATH)), DataFilePaths.USERS_PATH);
        UserFriendsCsvProcessor userFriendsCsvProcessor = new UserFriendsCsvProcessor(new CSVReader(
                new FileReader(DataFilePaths.USERS_FRIENDS_PATH)), DataFilePaths.USERS_FRIENDS_PATH);
        PersonalDebtsCsvProcessor personalDebtsCsvProcessor = new PersonalDebtsCsvProcessor(new CSVReader(
                new FileReader(DataFilePaths.PERSONAL_DEBTS_PATH)), DataFilePaths.PERSONAL_DEBTS_PATH);
        NotificationsCsvProcessor notificationCsvProcessor = new NotificationsCsvProcessor(new CSVReader(
                new FileReader(DataFilePaths.NOTIFICATIONS_PATH)), DataFilePaths.NOTIFICATIONS_PATH);
        GroupDebtsCsvProcessor groupDebtDTOCsvProcessor = new GroupDebtsCsvProcessor(new CSVReader(
                new FileReader(DataFilePaths.GROUP_DEBTS_PATH)), DataFilePaths.GROUP_DEBTS_PATH);
        FriendGroupsCsvProcessor friendGroupCsvProcessor = new FriendGroupsCsvProcessor(new CSVReader(
                new FileReader(DataFilePaths.FRIEND_GROUPS_PATH)), DataFilePaths.FRIEND_GROUPS_PATH);
        ExpensesCsvProcessor expenseCsvProcessor = new ExpensesCsvProcessor(new CSVReader(
                new FileReader(DataFilePaths.EXPENSES_PATH)), DataFilePaths.EXPENSES_PATH);
        dependencyContainer.register(UserCsvProcessor.class, userCsvProcessor)
                .register(UserFriendsCsvProcessor.class, userFriendsCsvProcessor)
                .register(PersonalDebtsCsvProcessor.class, personalDebtsCsvProcessor)
                .register(NotificationsCsvProcessor.class, notificationCsvProcessor)
                .register(GroupDebtsCsvProcessor.class, groupDebtDTOCsvProcessor)
                .register(FriendGroupsCsvProcessor.class, friendGroupCsvProcessor)
                .register(ExpensesCsvProcessor.class, expenseCsvProcessor);
    }

    private void registerRepositories() {
        UserRepository userRepository = new DefaultUserRepository(dependencyContainer);
        dependencyContainer.register(UserRepository.class, userRepository);
        UserFriendsRepository userFriendsRepository = new DefaultUserFriendsRepository(dependencyContainer);
        dependencyContainer.register(UserFriendsRepository.class, userFriendsRepository);
        NotificationsRepository notificationsRepository = new DefaultNotificationsRepository(dependencyContainer);
        dependencyContainer.register(NotificationsRepository.class, notificationsRepository);
        PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(dependencyContainer);
        dependencyContainer.register(PersonalDebtsRepository.class, personalDebtsRepository);
        FriendGroupRepository friendGroupRepository = new DefaultFriendGroupRepository(dependencyContainer);
        dependencyContainer.register(FriendGroupRepository.class, friendGroupRepository);
        GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(dependencyContainer);
        dependencyContainer.register(GroupDebtsRepository.class, groupDebtsRepository);
        ExpensesRepository expensesRepository = new DefaultExpensesRepository(dependencyContainer);
        dependencyContainer.register(ExpensesRepository.class, expensesRepository);
    }

    public void start() {
        Logger logger = Logger.getLogger(SplitwiseServer.class.getName());
        logger.addHandler(getLoggerHandler());
        dependencyContainer.register(Logger.class, logger);

        while (true) {
            try (ServerSocket serverSocket = new ServerSocket()) {
                serverSocket.bind(new InetSocketAddress(HOST, PORT));

                handleFiles();
                registerCSVProcessors();
                registerRepositories();
                InetSocketAddress address = new InetSocketAddress(serverSocket.getInetAddress(), PORT);
                dependencyContainer
                        .register(ChatRepository.class, new DefaultChatRepository(dependencyContainer, address));

                while (true) {
                    Socket client = serverSocket.accept();
                    Authenticator authenticator = new DefaultAuthenticator(dependencyContainer, client);
                    ChatToken chatToken = new DefaultChatToken(dependencyContainer, authenticator);
                    CommandFactory commandFactory = new CommandFactory(dependencyContainer, authenticator, chatToken);
                    executorService.execute(() ->
                            new ClientRequestHandler(dependencyContainer, client, commandFactory).run());
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.severe(e.getMessage());
            }
        }
    }
}

class Program {
    public static void main(String[] args) {
        new SplitwiseServer().start();
    }
}