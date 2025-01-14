package bg.sofia.uni.fmi.mjt.poll.server.command;

import bg.sofia.uni.fmi.mjt.poll.server.repository.PollRepository;
import bg.sofia.uni.fmi.mjt.poll.server.response.Response;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public interface Command {
    Response execute();

    static Command of(String input, PollRepository repository, SocketChannel client) {
        Stream<String> stream =
                Arrays.stream(input.strip().split("[\s]+"))
                        .filter(s -> !s.isEmpty() && !s.isBlank());
        ArrayList<String> splitted = new ArrayList<>(stream.toList());
        String commandName = splitted.get(0);
        splitted.remove(0);

        return switch (commandName) {
            case "create-poll" -> new CreateCommand(splitted, repository);
            case "list-polls" -> new ListCommand(repository);
            case "submit-vote" -> new SubmitVoteCommand(splitted, repository);
            case "disconnect" -> new DisconnectCommand(client);
            case null, default -> null;
        };
    }
}
