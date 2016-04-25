package org.gearvrf.debug.cli;

public interface LineProcessor {
    /**
     * Executes a command and returns the output.
     *
     * @param line The command line.
     * @return The output string. {@code null} if the session is ended.
     */
    String processLine(String line);

    /**
     * Gets the prompt of the line processor.
     * @return The prompt string.
     */
    String getPrompt();
}
