/*
 * This file is part of the Cliche project, licensed under MIT License.
 * See LICENSE.txt file in root folder of Cliche sources.
 */

package org.gearvrf.debug.cli;

/**
 * This interface is used by the Shell to support new argument types.
 * It converts string to an object of given class.
 * @author ASG
 */
public interface InputConverter {
    /**
     * String-to-someClass conversion method
     * May throw any exception if string is considered invalid for given class;
     * must do nothing but return null if doesn't recognize the toClass.
     * @param original String to be converted
     * @param toClass Class to be converted to
     * @return Object of the class toClass or <strong>null</strong>, if don't know how to convert to given class
     *
     * @see org.gearvrf.debug.cli.Shell
     */
    Object convertInput(String original, Class toClass) throws Exception;
}
