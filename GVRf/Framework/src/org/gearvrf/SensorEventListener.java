package org.gearvrf;

/**
 * Use this listener to receive {@link SensorEvent}s for a given
 * {@link GVRBaseSensor}.
 * 
 * Note that the callbacks happen on the main UI thread.
 *
 */
public interface SensorEventListener {
    /**
     * The callback that returns the {@link SensorEvent}s for the corresponding
     * {@link GVRBaseSensor}.
     * 
     * @param event
     *            The {@link SensorEventListener} returns one
     *            {@link SensorEvent} for every affected object. Use the
     *            methods in {@link SensorEvent} to retrieve the information
     *            contained in the event.
     */
    public void onSensorEvent(SensorEvent event);
}
