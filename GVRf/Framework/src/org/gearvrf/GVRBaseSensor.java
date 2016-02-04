/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gearvrf.io.GVRInputManager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

/**
 * Create an instance of this class to receive {@link SensorEvent}s whenever an
 * input device interacts with a {@link GVRSceneObject}.
 * 
 * Note that to successfully receive {@link SensorEvent}s for an object make
 * sure that the sensor is enabled and a valid {@link ISensorEvents} is
 * attached.
 * 
 */
public class GVRBaseSensor {
    private static final String TAG = GVRBaseSensor.class.getSimpleName();
    private static final float[] EMPTY_HIT_POINT = new float[3];
    private boolean enabled = true;
    private ListenerDelegate listener;
    private SparseArray<ControllerData> controllerData;
    protected GVRContext gvrContext;

    public GVRBaseSensor(GVRContext gvrContext) {
        this(gvrContext, null);
    }

    public GVRBaseSensor(GVRContext gvrContext, ISensorEvents listener) {
        this.gvrContext = gvrContext;
        this.listener = new ListenerDelegate(listener);
        controllerData = new SparseArray<GVRBaseSensor.ControllerData>();
    }

    void setActive(GVRCursorController controller, boolean active) {
        getControllerData(controller).setActive(active);
    }

    void addSceneObject(GVRCursorController controller, GVRSceneObject object,
            float[] hitPoint) {
        ControllerData data = getControllerData(controller);
        Set<GVRSceneObject> prevHits = data.getPrevHits();
        List<SensorEvent> newHits = data.getNewHits();

        if (prevHits.contains(object)) {
            prevHits.remove(object);
        }

        SensorEvent event = SensorEvent.obtain();
        event.setObject(object);
        event.setHitPoint(hitPoint);
        event.setOver(true);
        newHits.add(event);
    }

    void processList(GVRCursorController controller) {
        final List<SensorEvent> events = new ArrayList<SensorEvent>();

        ControllerData data = getControllerData(controller);
        Set<GVRSceneObject> prevHits = data.getPrevHits();
        List<SensorEvent> newHits = data.getNewHits();

        // process the previous hit objects to set isOver to false.
        if (prevHits.isEmpty() == false) {
            for (GVRSceneObject object : prevHits) {
                SensorEvent event = SensorEvent.obtain();
                event.setActive(data.getActive());
                event.setCursorController(controller);
                event.setObject(object);
                // clear the hit point
                event.setHitPoint(EMPTY_HIT_POINT);
                event.setOver(false);
                events.add(event);
            }
            if (data.getActive() == false) {
                prevHits.clear();
            }
        }

        if (newHits.isEmpty() == false) {
            for (SensorEvent event : newHits) {
                event.setActive(data.getActive());
                event.setCursorController(controller);
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

    ControllerData getControllerData(GVRCursorController controller) {
        ControllerData data = controllerData.get(controller.getId());
        if (data == null) {
            data = new ControllerData();
            controllerData.append(controller.getId(), data);
        }
        return data;
    }

    /**
     * Register a listener to receive updates about objects tagged to this
     * sensor.
     * 
     * Note that the method calls happen on the UI Thread.
     * 
     * @param listener
     */
    public void registerSensorEventListener(ISensorEvents listener) {
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

    private class ListenerDelegate {
        private final ISensorEvents sensorEventListener;
        private final Handler mainThreadHandler;

        public ListenerDelegate(ISensorEvents listener) {
            this.sensorEventListener = listener;
            mainThreadHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    SensorEvent event = (SensorEvent) msg.obj;

                    // Sends the onSensorEvent event
                    GVREventManager eventManager = gvrContext.getEventManager();
                    eventManager.sendEvent(sensorEventListener,
                            ISensorEvents.class, "onSensorEvent", event);

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

    /**
     * This class keeps track of the all the events generated by a
     * {@link GVRCursorController} on a given {@link GVRBaseSensor}.
     */
    private static class ControllerData {
        private Set<GVRSceneObject> prevHits;
        private List<SensorEvent> newHits;
        private boolean active;

        public ControllerData() {
            prevHits = new HashSet<GVRSceneObject>();
            newHits = new ArrayList<SensorEvent>();
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public Set<GVRSceneObject> getPrevHits() {
            return prevHits;
        }

        public List<SensorEvent> getNewHits() {
            return newHits;
        }

        public boolean getActive() {
            return active;
        }
    }
}