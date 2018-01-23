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

import android.view.MotionEvent;

/**
 * Create an instance of this class to receive {@link SensorEvent}s whenever an
 * input device interacts with a {@link GVRSceneObject}.
 * <p>
 * Sensor events are generated when the active picker is inside a collider
 * that is attached to a descendant of the sensor's owner. Thus, a single
 * sensor may get events from multiple colliders.
 * <p>
 * Note that to successfully receive {@link SensorEvent}s for an object make
 * sure that the sensor is enabled and a valid {@link ISensorEvents} is
 * attached.
 * <p>
 * To respond to sensor events from a scene object, attach an instance of {@link ISensorEvents}
 * as a listener to the {@link GVREventReceiver} of the {@link GVRSceneObject}) using
 * {@link GVRSceneObject#getEventReceiver()} to get the {@link GVREventReceiver}, and then
 * calling {@link GVREventReceiver#addListener(IEvents)} to add the {@link ISensorEvents}.
 * @see IPickEvents
 * @see ISensorEvents
 */
public class GVRBaseSensor extends GVRBehavior
{
    private static final String TAG = GVRBaseSensor.class.getSimpleName();
    static private long TYPE_SENSOR = newComponentType(GVRBaseSensor.class);

    private static final float[] EMPTY_HIT_POINT = new float[3];
    private GVRPicker.GVRPickedObject mHit = null;

    /*
     * Listens for pick events from all scene objects.
     * If the owner of a hit object or one of its ancestors
     * has a sensor, a sensor event is emitted to the
     * owner of that sensor. This propagates collisions
     * up the scene graph to the closest ancestor
     * with a sensor.
     */
    static final private ITouchEvents sPickHandler = new ITouchEvents ()
    {
        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision)
        {
            GVRBaseSensor sensor = findSensor(sceneObj);
            if (sensor != null)
            {
                sensor.sendSensorEvent(collision, true);
            }
        }

        public void onTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision)
        {
            GVRBaseSensor sensor = findSensor(sceneObj);
            if (sensor != null)
            {
                sensor.sendSensorEvent(collision, true);
            }
        }

        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision)
        {
            GVRBaseSensor sensor = findSensor(sceneObj);
            if (sensor != null)
            {
                sensor.sendSensorEvent(collision, false);
            }
        }

        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision)
        {
            GVRBaseSensor sensor = findSensor(sceneObj);
            if (sensor != null)
            {
                sensor.sendSensorEvent(collision, true);
            }
        }

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision)
        {
            GVRBaseSensor sensor = findSensor(sceneObj);
            if (sensor != null)
            {
                sensor.sendSensorEvent(collision, true);
            }
        }

        public void onMotionOutside(GVRPicker picker, MotionEvent event) { }

        /*
         * Scan up the scene graph from the object hit
         * to find a parent with a sensor attached.
         */
        public GVRBaseSensor findSensor(GVRSceneObject obj)
        {
            while (obj != null)
            {
                GVRBaseSensor sensor = obj.getSensor();

                if (sensor != null)
                {
                    return sensor;
                }
                obj = obj.getParent();
            }
            return null;
        }
    };


    /**
     * Constructor a sensor which emits {@link ISensorEvents} to
     * the owning scene object based on {@link ITouchEvents}.
     * @param gvrContext the {@link GVRContext} associated with the application.
     */
    public GVRBaseSensor(GVRContext gvrContext)
    {
        super(gvrContext, 0L);
        mType = TYPE_SENSOR;
        mIsListening = false;
    }

    /**
     * Constructor a sensor which emits {@link ISensorEvents} to
     * the owning scene object based on {@link ITouchEvents}.
     * The touch events may optionally be routed to the scene object.
     * @param gvrContext the {@link GVRContext} associated with the application.
     * @param sendTouchEvents true to send the touch events to the owning scene object
     */
    public GVRBaseSensor(GVRContext gvrContext, boolean sendTouchEvents)
    {
        super(gvrContext, 0L);
        mType = TYPE_SENSOR;
        mIsListening = false;
    }

    static public long getComponentType()
    {
        return TYPE_SENSOR;
    }

    /**
     * Get the global pick handler for sensors.
     * It dispatches picking events for each collider
     * to the proper sensor. It is added as a listener
     * to the main GVRCursorController by GVRINputManager.
     *
     * @return sensor pick handler
     */
    static public final ITouchEvents getPickHandler() { return sPickHandler; }

    /*
     * Send a sensor event to the owner of this sensor.
     */
    private void sendSensorEvent(GVRPicker.GVRPickedObject collision, boolean over)
    {
        SensorEvent event = SensorEvent.obtain();
        final IEventReceiver ownerCopy = getOwnerObject();
        GVRCursorController controller;

        if (collision.picker == null)
        {
            collision.picker = mHit.picker;
        }
        else if (over)
        {
            mHit = collision;
        }
        controller = collision.picker.getController();
        if (controller != null)
        {
            event.setCursorController(controller);
        }
        event.setActive(collision.touched);
        event.setPickedObject(collision);
        event.setOver(over);
        getGVRContext().getEventManager().sendEvent(ownerCopy, ISensorEvents.class, "onSensorEvent", event);
        event.recycle();
    }
}