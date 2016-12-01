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

import android.util.SparseArray;

import org.gearvrf.SensorEvent.EventGroup;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Create an instance of this class to receive {@link SensorEvent}s whenever an
 * input device interacts with a {@link GVRSceneObject}. <p>
 *
 * Note that to successfully receive {@link SensorEvent}s for an object make
 * sure that the sensor is enabled and a valid {@link ISensorEvents} is
 * attached. <p>
 *
 * To attach a {@link ISensorEvents}, set the sensor to the object (e.g., {@link GVRSceneObject}),
 * use {@link GVRSceneObject#getEventReceiver()} to get the {@link GVREventReceiver}, and then
 * use {@link GVREventReceiver#addListener(IEvents)} to add the {@link ISensorEvents}. <p>
 */
public class GVRBaseSensor {
    private static final String TAG = GVRBaseSensor.class.getSimpleName();
    private static final float[] EMPTY_HIT_POINT = new float[3];
    private boolean enabled = true;
    private SparseArray<ControllerData> controllerData;
    protected GVRContext gvrContext;
    protected IEventReceiver owner;
    private DepthComparator depthComparator;

    public GVRBaseSensor(GVRContext gvrContext) {
        this.gvrContext = gvrContext;
        controllerData = new SparseArray<GVRBaseSensor.ControllerData>();
        depthComparator = new DepthComparator();
    }

    void setActive(GVRCursorController controller, boolean active) {
        getControllerData(controller).setActive(active);
    }

    void addSceneObject(GVRCursorController controller, GVRSceneObject object,
            float hitX, float hitY, float hitZ) {
        ControllerData data = getControllerData(controller);
        Set<GVRSceneObject> prevHits = data.getPrevHits();
        List<SensorEvent> newHits = data.getNewHits();

        if (prevHits.contains(object)) {
            prevHits.remove(object);
        }

        SensorEvent event = SensorEvent.obtain();
        event.setObject(object);
        event.setHitPoint(hitX, hitY, hitZ);
        event.setOver(true);
        newHits.add(event);
    }

    boolean processList(GVRCursorController controller) {
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
                event.setHitPoint(EMPTY_HIT_POINT[0], EMPTY_HIT_POINT[1], EMPTY_HIT_POINT[2]);
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
        boolean eventHandled = false;
        GVREventManager eventManager = gvrContext.getEventManager();
        if (events.isEmpty() == false) {
            Collections.sort(events, depthComparator);
            final IEventReceiver ownerCopy = owner;
            if(events.size() > 1) {
                for(int i=0; i < events.size(); i++) {
                    SensorEvent event = events.get(i);
                    if(i==0) {
                        event.setEventGroup(EventGroup.MULTI_EVENT_START);
                    } else if(i == events.size()-1) {
                        event.setEventGroup(EventGroup.MULTI_EVENT_STOP);
                    } else {
                        event.setEventGroup(EventGroup.MULTI_EVENT);
                    }
                    eventHandled = eventManager.sendEvent(ownerCopy, ISensorEvents.class,
                            "onSensorEvent", event);
                    event.recycle();
                }
            } else {
                SensorEvent event = events.get(0);
                event.setEventGroup(EventGroup.SINGLE);
                eventHandled = eventManager.sendEvent(ownerCopy, ISensorEvents.class,
                        "onSensorEvent", event);
                event.recycle();
            }
        }
        return eventHandled;
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

    /**
     * Gets the owner of the sensor. The owner of the sensor can receive
     * events from the sensor.
     *
     * @return The owner object.
     */
    protected IEventReceiver getOwner() {
        return owner;
    }

    /**
     * Sets the owner of the sensor. The owner of the sensor can receive
     * events from the sensor, and must implement the interface {@link IEventReceiver}.
     *
     * @param owner The owner object of the sensor.
     */
    protected void setOwner(IEventReceiver owner) {
        this.owner = owner;
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

    private static class DepthComparator implements Comparator<SensorEvent> {
        Vector3f originVector;
        DepthComparator() {
            originVector = new Vector3f(0,0,0);
        }
        @Override
        public int compare(SensorEvent lhs, SensorEvent rhs) {
            GVRTransform transform = lhs.getObject().getTransform();
            float lhsDepth = originVector.distance(transform.getPositionX(),transform
                    .getPositionY(), transform.getPositionZ());
            transform = rhs.getObject().getTransform();
            float rhsDepth = originVector.distance(transform.getPositionX(),transform
                    .getPositionY(), transform.getPositionZ());
            return (int)(lhsDepth - rhsDepth);
        }
    }
}