/*
 * This file is part of the Cliche project, licensed under MIT License.
 * See LICENSE.txt file in root folder of Cliche sources.
 */

package org.gearvrf.debug.cli;

import java.lang.reflect.Method;
import java.util.List;

import org.gearvrf.debug.cli.util.Strings;

/**
 * Default "dash-joined" implementation of the CommandNamer.
 *
 * @author ASG
 */
public class DashJoinedNamer implements CommandNamer {

    private final boolean doRemoveCommonPrefix;

    public DashJoinedNamer(boolean doRemoveCommonPrefix) {
        this.doRemoveCommonPrefix = doRemoveCommonPrefix;
    }

    public NamingInfo nameCommand(Method method) {
        List<String> words = Strings.splitJavaIdentifier(method.getName());

        if (doRemoveCommonPrefix) {
            final String COMMON_PREFIX_1 = "cmd";
            final String COMMON_PREFIX_2 = "cli";

            if (words.size() > 1 && (words.get(0).equals(COMMON_PREFIX_1)
                    || words.get(0).equals(COMMON_PREFIX_2))) {
                words.remove(0);
            }
        }

        String name = Strings.joinStrings(words, true,'-');
        String[] abbrevs = proposeAbbrevs(words);
        return new NamingInfo(name, abbrevs);
    }

    private String[] proposeAbbrevs(List<String> words) {
        if (words.size() == 1 && words.get(0).equals("exit")) { // exit has reserved meaning; sorry for this ugly hack.
            return new String[]{ };
        }
        String abbrev1 = "";
        for (String word : words) {
            assert word.length() > 0;
            abbrev1 += Character.toLowerCase(word.charAt(0));
        }

        String abbrev2 = "";
        for (String word : words) {
            abbrev2 += Character.toLowerCase(word.charAt(0));
            if (word.length() > 1) {
                abbrev2 += Character.toLowerCase(word.charAt(1));
            }
        }

        if (!abbrev2.isEmpty()) {
            return new String[]{ abbrev1, abbrev2 };
        } else {
            return  new String[]{ abbrev1 };
        }

    }

}
