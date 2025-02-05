package bg.sofia.uni.fmi.mjt.splitwise.server.chat;

import java.util.concurrent.ThreadLocalRandom;

public class ChatCodeGenerator {
    private static final int ALPHABET_SIZE = 26;
    private static final int FIRST_PART_SIZE = 3;
    private static final int SECOND_PART_SIZE = 3;
    private static final int THIRD_PART_SIZE = 3;

    private StringBuilder generateLowercaseSequence(ThreadLocalRandom random, StringBuilder sb, int length) {
        for (int i = 0; i < length; i++) {
            sb.append((char)('a' + random.nextInt(ALPHABET_SIZE)));
        }
        return sb;
    }

    public String generateRandom() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder();
        generateLowercaseSequence(random, sb, FIRST_PART_SIZE).append('-');
        generateLowercaseSequence(random, sb, SECOND_PART_SIZE).append('-');
        generateLowercaseSequence(random, sb, THIRD_PART_SIZE);
        return sb.toString();
    }
}
