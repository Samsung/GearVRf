/*
 * This file is part of the Cliche project, licensed under MIT License.
 * See LICENSE.txt file in root folder of Cliche sources.
 */

package org.gearvrf.debug.cli;

/**
 * Classes that want to have an instance of Shell associated with them should implement this interface.
 * Upon registration in Shell, cliSetShell() is called.
 *
 * @author ASG
 */
public interface ShellDependent {

    /**
     * This method informs the object about the Shell operating it.
     * Is called upon object's registration in Shell.
     * @param theShell Shell running the object.
     */
    void cliSetShell(Shell theShell);

}
