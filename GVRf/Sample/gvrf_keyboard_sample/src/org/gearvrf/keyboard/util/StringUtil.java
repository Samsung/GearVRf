package org.gearvrf.keyboard.util;

import java.util.Vector;

public class StringUtil {

    public static Vector<StringBuffer> splitStringInLines(String baseString, int lineWidth) {
        int lettersCount = 0;
        int currentPosition = 0;
        String[] words = baseString.split(" ");
        Vector<StringBuffer> lines = new Vector<StringBuffer>();
        for (int i = 0; i < words.length; i++) {
            lettersCount += words[i].length();
            if (lettersCount > lineWidth) {
                StringBuffer questionLine = new StringBuffer();
                for (int j = currentPosition; j < i; j++) {
                    questionLine.append(words[j]);
                    if (j < i - 1) {
                        questionLine.append(" ");
                    }
                }
                lines.add(questionLine);

                currentPosition = i--;
                lettersCount = 0;
            }
        }

        if (currentPosition < words.length) {
            StringBuffer questionLine = new StringBuffer();
            for (int i = currentPosition; i < words.length; i++) {
                questionLine.append(words[i]);
                if (i < words.length - 1) {
                    questionLine.append(" ");
                }
            }
            lines.add(questionLine);
        }
        return lines;
    }
    
}
