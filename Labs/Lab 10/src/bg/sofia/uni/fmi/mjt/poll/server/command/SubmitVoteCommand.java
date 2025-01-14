package bg.sofia.uni.fmi.mjt.poll.server.command;

import bg.sofia.uni.fmi.mjt.poll.server.repository.PollRepository;
import bg.sofia.uni.fmi.mjt.poll.server.response.Response;
import bg.sofia.uni.fmi.mjt.poll.server.response.StatusCode;

import java.util.List;

public class SubmitVoteCommand implements Command {
    private List<String> commandLine;
    private final PollRepository repository;

    public SubmitVoteCommand(List<String> commandLine, PollRepository repository) {
        this.commandLine = commandLine;
        this.repository = repository;
    }

    private int parseID(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException | NullPointerException e) {
            return -1;
        }
    }

    @Override
    public Response execute() {
        if (commandLine.isEmpty()) {
            return new Response(StatusCode.ERROR,
                    "message",
                    "\"Command line should contain the id of the poll\"");
        }
        int pollID = parseID(commandLine.get(0));
        if (commandLine.size() == 1) {
            return new Response(StatusCode.ERROR,
                    "message",
                    "\"Command line should contain the option\"");
        }
        String option = commandLine.get(1);
        if (repository.getPoll(pollID) == null) {
            return new Response(StatusCode.ERROR,
                    "message",
                    "\"Poll with ID " + pollID + " does not exist.\"");
        }
        if (!repository.getPoll(pollID).options().containsKey(option)) {
            return new Response(StatusCode.ERROR,
                    "message",
                    "\"Invalid option. Option " + option + " does not exist.\"");
        }

        var options = repository.getPoll(pollID).options();
        options.put(option, options.get(option) + 1);
        StringBuilder statusMessageBuilder = new StringBuilder();
        statusMessageBuilder.append("\"Vote submitted successfully for option: ").append(option);
        return new Response(StatusCode.OK, "message", statusMessageBuilder.append("\"").toString());
    }
}