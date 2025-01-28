package bg.sofia.uni.fmi.mjt.splitwise.server;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.DefaultAuthenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.token.ChatToken;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.token.DefaultChatToken;
import bg.sofia.uni.fmi.mjt.splitwise.server.command.factory.CommandFactory;
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SplitwiseServer {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(HOST, PORT));

            UserRepository userRepository = new DefaultUserRepository();
            UserFriendsRepository userFriendsRepository = new DefaultUserFriendsRepository(userRepository);
            PersonalDebtsRepository personalDebtsRepository = new DefaultPersonalDebtsRepository(userRepository);
            NotificationsRepository notificationsRepository = new DefaultNotificationsRepository(userRepository);
            FriendGroupRepository friendGroupRepository = new DefaultFriendGroupRepository(userRepository, userFriendsRepository);
            GroupDebtsRepository groupDebtsRepository = new DefaultGroupDebtsRepository(userRepository, friendGroupRepository);
            ExpensesRepository expensesRepository = new DefaultExpensesRepository(userRepository, friendGroupRepository, personalDebtsRepository, groupDebtsRepository);
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

class Program {
    public static void main(String[] args) {
        new SplitwiseServer().start();
    }
}