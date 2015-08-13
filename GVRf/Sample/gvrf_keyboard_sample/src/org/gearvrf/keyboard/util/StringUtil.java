/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
