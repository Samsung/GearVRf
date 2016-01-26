/*
 * This file is part of the Cliche project, licensed under MIT License.
 * See LICENSE.txt file in root folder of Cliche sources.
 */

package org.gearvrf.debug.cli;

/**
 *
 * Root exception for Cliche.
 *
 * @author ASG
 */
public class CLIException extends Exception {
    public CLIException() {
        super();
    }
    public CLIException(String message) {
        super(message);
    }
    public CLIException(Throwable cause) {
        super(cause);
    }
    public CLIException(String message, Throwable cause) {
        super(message, cause);
    }

    public static CLIException createCommandNotFound(String commandName) {
        return new CLIException("Unknown command: " + Token.escapeString(commandName));
    }

    public static CLIException createCommandNotFoundForArgNum(String commandName, int argCount) {
        return new CLIException("There's no command " + Token.escapeString(commandName)
                + " taking " + argCount + " arguments");
    }

    public static CLIException createAmbiguousCommandExc(String commandName, int argCount) {
        return new CLIException("Ambiguous command " + Token.escapeString(commandName)
                + " taking " + argCount + " arguments");
    }
}
