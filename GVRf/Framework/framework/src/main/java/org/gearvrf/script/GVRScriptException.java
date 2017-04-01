package org.gearvrf.script;

@SuppressWarnings("serial")
/**
 * Exception thrown while executing a script.
 */
public class GVRScriptException extends Exception {
    public GVRScriptException(String exceptionString) {
        super(exceptionString);
    }
}
