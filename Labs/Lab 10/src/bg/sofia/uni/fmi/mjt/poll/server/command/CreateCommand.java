package bg.sofia.uni.fmi.mjt.poll.server.command;

import bg.sofia.uni.fmi.mjt.poll.server.model.Poll;
import bg.sofia.uni.fmi.mjt.poll.server.repository.PollRepository;
import bg.sofia.uni.fmi.mjt.poll.server.response.Response;
import bg.sofia.uni.fmi.mjt.poll.server.response.StatusCode;

import java.util.List;
import java.util.stream.Collectors;

public class CreateCommand implements Command {
    private List<String> commandLine;
    private final PollRepository repository;

    public CreateCommand(List<String> commandLine, PollRepository repository) {
        this.commandLine = commandLine;
        this.repository = repository;
    }

    @Override
    public Response execute() {
        if (commandLine.isEmpty()) {
            return new Response(StatusCode.ERROR,
                    "message",
                    "Command line should contain the title of the question");
        }
        String content = commandLine.get(0);
        if (commandLine.size() <= 2) {
            return new Response(StatusCode.ERROR,
                    "message",
                    "The question should contain at least 2 responses");
        }
        List<String> options = commandLine.stream().skip(1).toList();
        Poll poll = new Poll(content, options
                .stream()
                .collect(Collectors.toMap(key -> key, val -> 0)));
        int id = repository.addPoll(poll);

        StringBuilder statusMessageBuilder = new StringBuilder();
        statusMessageBuilder
                .append("\"Poll ")
                .append(id)
                .append(" created successfully.\"");
        return new Response(StatusCode.OK, "message", statusMessageBuilder.toString());
    }
}
