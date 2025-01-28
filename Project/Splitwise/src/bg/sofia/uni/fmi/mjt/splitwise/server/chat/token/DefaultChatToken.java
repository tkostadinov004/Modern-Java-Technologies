package bg.sofia.uni.fmi.mjt.splitwise.server.chat.token;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.exception.NotAuthenticatedException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.exception.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.ChatServer;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ChatRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.UserRepository;

public class DefaultChatToken implements ChatToken {
    private final Authenticator authenticator;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private ChatServer chatServer;

    public DefaultChatToken( Authenticator authenticator, UserRepository userRepository, ChatRepository chatRepository) {
        this.authenticator = authenticator;
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
    }

    @Override
    public boolean isInChat() {
        return chatServer != null;
    }

    @Override
    public ChatServer getServer() throws ChatException {
        if (!isInChat()) {
            throw new ChatException("User is not currently in chat!");
        }

        return chatServer;
    }

    @Override
    public void joinChat(String chatCode) throws ChatException, NotAuthenticatedException {
        if (!authenticator.isAuthenticated()) {
            throw new NotAuthenticatedException("User is not authenticated!");
        }
        if (isInChat()) {
            throw new ChatException("User is already in chat!");
        }

        chatRepository.connectUser(authenticator.getAuthenticatedUser().username(), chatCode);
        chatServer = chatRepository.getByCode(chatCode).get();
    }

    @Override
    public void leaveChat() throws ChatException {
        if (!isInChat()) {
            throw new ChatException("User is not in a chat!");
        }

        chatRepository.disconnectUser(authenticator.getAuthenticatedUser().username(), chatServer.code());
        this.chatServer = null;
    }
}
