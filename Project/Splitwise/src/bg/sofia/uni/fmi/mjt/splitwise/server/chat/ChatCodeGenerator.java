package bg.sofia.uni.fmi.mjt.splitwise.server.chat;

import java.util.concurrent.ThreadLocalRandom;

public class ChatCodeGenerator {
    private static final int ALPHABET_SIZE = 26;

    private StringBuilder generateLowercaseSequence(ThreadLocalRandom random, StringBuilder sb, int length) {
        for (int i = 0; i < length; i++) {
            sb.append((char)('a' + random.nextInt(ALPHABET_SIZE)));
        }
        return sb;
    }

    public String generateRandom() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder();
        generateLowercaseSequence(random, sb, 3).append('-');
        generateLowercaseSequence(random, sb, 4).append('-');
        generateLowercaseSequence(random, sb, 3);
        return sb.toString();
    }
}
