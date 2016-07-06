package org.gearvrf;

/**
 * Interface for events generated during an error condition.
 *
 * These events can come from anywhere in the system.
 * They are typically routed through the context and the scene object.
 *
 */
public interface IErrorEvents extends IEvents
{
    public void onError(String message, Object sender);
}
