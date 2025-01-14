package bg.sofia.uni.fmi.mjt.poll.server.command;

import bg.sofia.uni.fmi.mjt.poll.server.model.Poll;
import bg.sofia.uni.fmi.mjt.poll.server.repository.PollRepository;
import bg.sofia.uni.fmi.mjt.poll.server.response.Response;
import bg.sofia.uni.fmi.mjt.poll.server.response.StatusCode;

public class ListCommand implements Command {
    private final PollRepository repository;

    public ListCommand(PollRepository repository) {
        this.repository = repository;
    }

    private String stringifyPoll(Poll poll, int id) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"").append(id).append("\":{\"question\":\"").append(poll.question());
        sb.append("\",\"options\":{");
        for (var option : poll.options().entrySet()) {
            sb.append("\"").append(option.getKey()).append("\":").append(option.getValue());
            sb.append(",");
        }
        if (!poll.options().isEmpty()) {
            sb.append("\b");
        }
        sb.append("}}");

        return sb.toString();
    }

    @Override
    public Response execute() {
        if (repository.getAllPolls().isEmpty()) {
            return new Response(StatusCode.ERROR, "message",
                    "\"No active polls available.\"");
        }

        StringBuilder statusMessageBuilder = new StringBuilder();
        statusMessageBuilder.append("{");
        for (var poll : repository.getAllPolls().entrySet()) {
            statusMessageBuilder.append(stringifyPoll(poll.getValue(), poll.getKey()));
        }
        statusMessageBuilder.append("}");

        return new Response(StatusCode.OK, "polls", statusMessageBuilder.toString());
    }
}
