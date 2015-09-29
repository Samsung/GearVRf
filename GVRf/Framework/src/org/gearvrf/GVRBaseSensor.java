package org.gearvrf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Create an instance of this class to receive {@link SensorEvent}s whenever an
 * input device interacts with a {@link GVRSceneObject}.
 * 
 * Note that to successfully receive {@link SensorEvent}s for an object make
 * sure that the sensor is enabled and a valid {@link SensorEventListener} is
 * attached.
 * 
 */
public class GVRBaseSensor {
    private boolean enabled = true;
    private Set<GVRSceneObject> prevHits;
    private List<SensorEvent> newHits;
    private ListenerDelegate listener;

    private boolean active = false;

    public GVRBaseSensor(GVRContext gvrContext) {
        this(gvrContext, null);
    }

    public GVRBaseSensor(GVRContext gvrContext, SensorEventListener listener) {
        prevHits = new HashSet<GVRSceneObject>();
        newHits = new ArrayList<SensorEvent>();
        this.listener = new ListenerDelegate(listener);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    void addSceneObject(GVRSceneObject object, float[] hitPoint) {
        if (prevHits.contains(object)) {
            prevHits.remove(object);
        }

        SensorEvent event = SensorEvent.obtain();
        event.setObject(object);
        event.setHitPoint(hitPoint);
        event.setOver(true);
        newHits.add(event);
    }

    void processList(int pointerId) {
        final List<SensorEvent> events = new ArrayList<SensorEvent>();
        // process the previous hit objects to set isOver to false.
        if (prevHits.isEmpty() == false) {
            for (GVRSceneObject object : prevHits) {
                SensorEvent event = SensorEvent.obtain();
                event.setActive(active);
                event.setObject(object);
                event.setOver(false);
                events.add(event);

            }
            if (active == false) {
                prevHits.clear();
            }
        }

        if (newHits.isEmpty() == false) {
            for (SensorEvent event : newHits) {
                event.setActive(active);
                event.setPointerId(pointerId);
                events.add(event);
                prevHits.add(event.getObject());
            }
            newHits.clear();
        }

        if (events.isEmpty() == false && listener != null) {
            for (SensorEvent event : events) {
                listener.onSensorEvent(event);
            }
        }
    }

    /**
     * Register a listener to receive updates about objects tagged to this
     * sensor.
     * 
     * Note that the method calls happen on the UI Thread.
     * 
     * @param listener
     */
    public void registerSensorEventListener(SensorEventListener listener) {
        this.listener = new ListenerDelegate(listener);
    }

    /**
     * Use this method to disable the sensor.
     * 
     * Does not affect the sensor if already disabled.
     */
    public void disable() {
        enabled = false;
    }

    /**
     * Use this method to enable the sensor.
     * 
     * Does not affect the sensor if already enabled.
     */
    public void enable() {
        enabled = true;
    }

    /**
     * Get the status of the sensor using this call.
     * 
     * @return <code>true</code> if the sensor is enabled. <code>false</code> if
     *         not.
     */
    public boolean isEnabled() {
        return enabled;
    }

    private static class ListenerDelegate {
        private final SensorEventListener sensorEventListener;
        private final Handler mainThreadHandler;

        public ListenerDelegate(SensorEventListener listener) {
            this.sensorEventListener = listener;
            mainThreadHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    SensorEvent event = (SensorEvent) msg.obj;
                    sensorEventListener.onSensorEvent(event);
                    event.recycle();
                }
            };
        }

        public void onSensorEvent(SensorEvent event) {
            Message message = Message.obtain();
            message.obj = event;
            mainThreadHandler.dispatchMessage(message);
        }
    }

}
