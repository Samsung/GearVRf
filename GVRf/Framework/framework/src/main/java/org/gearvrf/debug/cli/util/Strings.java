/*
 * This file is part of the Cliche project, licensed under MIT License.
 * See LICENSE.txt file in root folder of Cliche sources.
 */

package org.gearvrf.debug.cli.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Procedural class with static public methods for string handling.
 * @author ASG
 */
public class Strings {

    /**
     * Fixes case of a word: Str -> str, but URL -> URL.
     * @param s Word to be fixed
     * @return all-lowercase or all-uppercase word.
     */
    public static String fixCase(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        if ( Character.isUpperCase(s.charAt(0))
                && (s.length() == 1 || Character.isLowerCase(s.charAt(1)))) {
            s = s.toLowerCase();
        }
        return s;
    }

    /**
     * Generic string joining function.
     * @param strings Strings to be joined
     * @param fixCase does it need to fix word case
     * @param withChar char to join strings with.
     * @return joined-string
     */
    public static String joinStrings(List<String> strings, boolean fixCase, char withChar) {
        if (strings == null || strings.size() == 0) {
            return "";
        }
        StringBuilder result = null;
        for (String s : strings) {
            if (fixCase) {
                s = fixCase(s);
            }
            if (result == null) {
                result = new StringBuilder(s);
            } else {
                result.append(withChar);
                result.append(s);
            }
        }
        return result.toString();
    }

    /**
     * Rather clever function. Splits javaCaseIdentifier into parts
     * (java, Case, Identifier).
     * @param string String to be splitted
     * @return List of components
     */
    public static List<String> splitJavaIdentifier(String string) {
        assert string != null;
        List<String> result = new ArrayList<String>();

        int startIndex = 0;
        while (startIndex < string.length()) {
            if (Character.isLowerCase(string.charAt(startIndex))) {
                int i = startIndex;
                while (i < string.length() && Character.isLowerCase(string.charAt(i))) {
                    i++;
                }
                result.add(string.substring(startIndex, i));
                startIndex = i;
            } else if (Character.isUpperCase(string.charAt(startIndex))) {
                if (string.length() - startIndex == 1) {
                    result.add(Character.toString(string.charAt(startIndex++)));
                } else if (Character.isLowerCase(string.charAt(startIndex + 1))) {
                    int i = startIndex + 1;
                    while (i < string.length() && Character.isLowerCase(string.charAt(i))) {
                        i++;
                    }
                    result.add(string.substring(startIndex, i));
                    startIndex = i;
                } else { // if there's several uppercase letters in row
                    int i = startIndex + 1;
                    while (i < string.length() && Character.isUpperCase(string.charAt(i))
                            && (string.length()-i == 1 || Character.isUpperCase(string.charAt(i+1)))) {
                        i++;
                    }
                    result.add(string.substring(startIndex, i));
                    startIndex = i;
                }
            }else {
                result.add(Character.toString(string.charAt(startIndex++)));
            }
        }
        return result;
    }

}
