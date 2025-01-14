package bg.sofia.uni.fmi.mjt.poll.server.repository;

import bg.sofia.uni.fmi.mjt.poll.server.model.Poll;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InMemoryPollRepository implements PollRepository {
    private Map<Integer, Poll> polls;
    private int biggestID;

    public InMemoryPollRepository() {
        polls = new HashMap<>();
        biggestID = 0;
    }

    @Override
    public int addPoll(Poll poll) {
        polls.put(++biggestID, poll);
        return biggestID;
    }

    @Override
    public Poll getPoll(int pollId) {
        return polls.getOrDefault(pollId, null);
    }

    @Override
    public Map<Integer, Poll> getAllPolls() {
        return Collections.unmodifiableMap(polls);
    }

    @Override
    public void clearAllPolls() {
        polls.clear();
        biggestID = 0;
    }
}
