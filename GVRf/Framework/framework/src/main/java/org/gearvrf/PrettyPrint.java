package org.gearvrf;

/**
 * PrettyPrint interface
 */
public interface PrettyPrint {

    /**
     * Prints the object with indentation.
     * 
     * @param sb the {@code StringBuffer} to receive the output.
     * 
     * @param indent indentation in number of spaces
     */
    void prettyPrint(StringBuffer sb, int indent);
}
