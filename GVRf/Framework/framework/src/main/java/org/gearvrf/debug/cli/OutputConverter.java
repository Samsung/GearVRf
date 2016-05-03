/*
 * This file is part of the Cliche project, licensed under MIT License.
 * See LICENSE.txt file in root folder of Cliche sources.
 */

package org.gearvrf.debug.cli;

/**
 * This interface is used by the Shell to support new return types.
 * It converts objects to other objects (usually strings) that will be displayed.
 * @author ASG
 */
public interface OutputConverter {
    /**
     * Object-to--user-friendly-object (usually string) conversion method.
     * The method must check argument's class, since it will be fed virtually all
     * returned objects. Simply return null when not sure.
     * @param toBeFormatted Object to be displayed to the user
     * @return Object representing the object or Null if don't know how to make it.
     *         Do not return default toString() !!
     */
    Object convertOutput(Object toBeFormatted);
}
