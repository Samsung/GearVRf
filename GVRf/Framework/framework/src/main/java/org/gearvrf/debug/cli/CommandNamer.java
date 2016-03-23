/*
 * This file is part of the Cliche project, licensed under MIT License.
 * See LICENSE.txt file in root folder of Cliche sources.
 */

package org.gearvrf.debug.cli;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * This interface is a Strategy for auto-naming commands with no name specified
 * based on command's method. The algorithm is isolated because it's highly
 * subjective and therefore subject to changes. And, if you don't like
 * default dash-joined naming you can completely redefine the functionality.
 *
 * It is also responsible for generating several suggested abbreviations,
 * one from them may later be "approved" by CommandTable.
 *
 * @author ASG
 */
public interface CommandNamer {
    
    /**
     * Generate command name and suggested abbreviation variants.
     * Since this method is given a Method, not a string, the algorithm can
     * take parameter types into account.
     *
     * @param commandMethod Command method
     * @return asg.cliche.CommandNamer.NamingInfo containing generated name and abbrev array.
     */
    NamingInfo nameCommand(Method commandMethod);

    /**
     * Return value grouping structure for nameCommand().
     * I decided to return name and abbreviations together because in the default
     * algorithm they are generated simultaneously, and I think this approach is
     * better than having a stateful strategy.
     */
    public static class NamingInfo {
        public final String commandName;
        public final String[] possibleAbbreviations;

        public NamingInfo(String commandName, String[] possibleAbbreviations) {
            this.commandName = commandName;
            this.possibleAbbreviations = possibleAbbreviations;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final NamingInfo other = (NamingInfo)obj;
            if ((this.commandName == null) ? (other.commandName != null) : !this.commandName.equals(other.commandName)) {
                return false;
            }
            if (this.possibleAbbreviations != other.possibleAbbreviations &&
                    (this.possibleAbbreviations == null ||
                    !Arrays.equals(this.possibleAbbreviations, other.possibleAbbreviations))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 59 * hash + (this.commandName != null ? this.commandName.hashCode() : 0);
            hash = 59 * hash + (this.possibleAbbreviations != null ? Arrays.hashCode(this.possibleAbbreviations) : 0);
            return hash;
        }

        @Override
        public String toString() {
            return String.format("NamingInfo(%s, %s)", commandName, Arrays.toString(possibleAbbreviations));
        }
    }

}
