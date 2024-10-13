import java.lang.*;
import java.util.*;

public class TextJustifier {
    public static String[] justifyText(String[] words, int maxWidth) {
        if(words.length == 0) {
            return new String[]{};
        }
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            int currentWidth = maxWidth;
            int currentWordCount = 0;
            while(i + currentWordCount < words.length && currentWidth - words[i + currentWordCount].length() >= currentWordCount) {
                currentWidth -= words[i + currentWordCount].length();
                currentWordCount++;
            }

            int remainingSpaces = currentWidth;
            if(i + currentWordCount >= words.length) {
                for (int j = 0; j < currentWordCount; j++) {
                    builder.append(words[i + j] + " ");
                    remainingSpaces--;
                }
                builder.deleteCharAt(builder.length() - 1);
                builder.append(" ".repeat(Math.max(remainingSpaces + 1, 0)));
                break;
            }

            for (int j = 0; j < currentWordCount; j++) {
                builder.append(words[i + j]);
                int currentSpaces = (int)Math.ceil(remainingSpaces * 1.0 / Math.max(currentWordCount - j - 1, 1));
                builder.append(" ".repeat(currentSpaces));
                remainingSpaces -= currentSpaces;
            }
            builder.append('\n');
            i += currentWordCount - 1;
        }
        return builder.toString().split("\n");
    }
}
