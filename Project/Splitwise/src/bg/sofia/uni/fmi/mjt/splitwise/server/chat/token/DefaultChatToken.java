package bg.sofia.uni.fmi.mjt.splitwise.server.chat.token;

import bg.sofia.uni.fmi.mjt.splitwise.server.authentication.authenticator.Authenticator;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.exception.ChatException;
import bg.sofia.uni.fmi.mjt.splitwise.server.chat.DefaultChatServer;
import bg.sofia.uni.fmi.mjt.splitwise.server.dependency.DependencyContainer;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.contracts.ChatRepository;
import bg.sofia.uni.fmi.mjt.splitwise.server.repository.exception.NonExistentChatRoomException;

import java.util.Optional;

public class DefaultChatToken implements ChatToken {
    private final Authenticator authenticator;
    private final ChatRepository chatRepository;
    private DefaultChatServer chatServer;

    public DefaultChatToken(DependencyContainer dependencyContainer, Authenticator authenticator) {
        this.chatRepository = dependencyContainer.get(ChatRepository.class);
        this.authenticator = authenticator;
    }

    @Override
    public boolean isInChat() {
        return chatServer != null;
    }

    @Override
    public DefaultChatServer getServer() throws ChatException {
        if (!isInChat()) {
            throw new ChatException("User is not currently in chat!");
        }

        return chatServer;
    }

    @Override
    public void joinChat(String chatCode) throws ChatException {
        if (!authenticator.isAuthenticated()) {
            throw new ChatException("You are not authenticated!");
        }
        if (isInChat()) {
            throw new ChatException("You are already in a chat room!");
        }

        chatRepository.connectUser(authenticator.getAuthenticatedUser().username(), chatCode);

        Optional<DefaultChatServer> server = chatRepository.getByCode(chatCode);
        if (server.isEmpty()) {
            throw new NonExistentChatRoomException("Chat room with code %s doesn't exist!".formatted(chatCode));
        }

        chatServer = server.get();
    }

    @Override
    public void leaveChat() throws ChatException {
        if (!isInChat()) {
            throw new ChatException("You are not in a chat room!");
        }

        chatRepository.disconnectUser(authenticator.getAuthenticatedUser().username(), chatServer.code());
        this.chatServer = null;
    }
}
