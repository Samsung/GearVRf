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
import java.util.List;

import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRCursorType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.utility.Log;
import org.joml.Vector3f;

import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Define a class of type {@link GVRCursorController} to register a new cursor
 * controller with the {@link GVRInputManager}.
 * 
 * You can request for all the {@link GVRCursorController}s in the system by
 * querying the {@link GVRInputManager#getCursorControllers()} call.
 * 
 * Alternatively all notifications for {@link GVRCursorController}s attached or
 * detached from the framework can be obtained by registering a
 * {@link CursorControllerListener} with the {@link GVRInputManager}.
 * 
 * Make use of the {@link GVRCursorController#setSceneObject(GVRSceneObject)} to
 * add a Cursor for the controller in 3D space. The {@link GVRInputManager} will
 * manipulate the {@link GVRSceneObject} based on the input coming in to the
 * {@link GVRCursorController}.
 * 
 * Use the {@link GVRInputManager#addCursorController(GVRCursorController)} call
 * to add an external {@link GVRCursorController} to the framework.
 * 
 */
public abstract class GVRCursorController {
    private static final String TAG = GVRCursorController.class.getSimpleName();
    private static int uniqueControllerId = 0;
    private final int controllerId;
    private final GVRCursorType cursorType;
    private boolean previousActive;
    private ActiveState activeState = ActiveState.NONE;
    private boolean invalidate = false;
    private boolean active;
    private float nearDepth, farDepth = -Float.MAX_VALUE;
    private final Vector3f position, ray;
    private boolean enable = true;
    private KeyEvent keyEvent, processedKeyEvent;
    private MotionEvent motionEvent, processedMotionEvent;

    private GVRSceneObject sceneObject;
    private Object sceneObjectLock = new Object();
    private Object eventLock = new Object();
    private List<ControllerEventListener> controllerEventListeners;

    public GVRCursorController(GVRCursorType cursorType) {
        this.controllerId = uniqueControllerId;
        this.cursorType = cursorType;
        uniqueControllerId++;
        position = new Vector3f();
        ray = new Vector3f();
        controllerEventListeners = new ArrayList<ControllerEventListener>();
    }

    /**
     * Return the {@link GVRCursorType} associated with the
     * {@link GVRCursorController}.
     * 
     * In most cases, this method should return {@link GVRCursorType#EXTERNAL}.
     * {@link GVRCursorType#EXTERNAL} allows the input device to define its own
     * input behavior. If the device wishes to implement
     * {@link GVRCursorType#MOUSE} or {@link GVRCursorType#CONTROLLER} make sure
     * that the behavior is consistent with that defined in
     * {@link GVRMouseDeviceManager} and {@link GVRGamepadDeviceManager}.
     * 
     * @return the {@link GVRCursorType} for the {@link GVRCursorController}.
     * 
     * @deprecated Use {@link GVRCursorController#getCursorType()} instead.
     */
    public GVRCursorType getGVRCursorType() {
        return cursorType;
    }

    /**
     * Return an id that represent this {@link GVRCursorController}
     * 
     * @return an integer id that identifies this controller.
     */
    public int getId() {
        return controllerId;
    }

    enum ActiveState {
        ACTIVE_PRESSED, ACTIVE_RELEASED, NONE
    };

    ActiveState getActiveState() {
        return activeState;
    }

    /**
     * Use this method to set the active state of the
     * {@link GVRCursorController}.
     * 
     * @param active
     *            This flag is usually attached to a button press, it is up to
     *            the developer to map the designated button to the active flag.
     * 
     *            Eg. A Gamepad could attach {@link KeyEvent#KEYCODE_BUTTON_A}
     *            to active. Setting active to true would result in a
     *            {@link SensorEvent} with {@link SensorEvent#isActive()} as
     *            <code>true</code> the affected that {@link GVRSceneObject}.
     */
    protected void setActive(boolean active) {
        // check if the controller is enabled
        if (isEnabled() == false) {
            return;
        }

        this.active = active;
        invalidate = true;
    }

    /**
     * Set a {@link GVRSceneObject} to be controlled by the
     * {@link GVRCursorController}.
     * 
     * @param object
     *            the {@link GVRSceneObject}
     */
    public void setSceneObject(GVRSceneObject object) {
        synchronized (sceneObjectLock) {
            if (sceneObject != null) {
                // if there is already an attached scene object transfer the
                // position to the new one
                object.getTransform().setPosition(
                        sceneObject.getTransform().getPositionX(),
                        sceneObject.getTransform().getPositionY(),
                        sceneObject.getTransform().getPositionZ());
            } else {
                // use the exiting position from the controller
                object.getTransform().setPosition(position.x, position.y,
                        position.z);
            }
            sceneObject = object;
        }
    }

    public void resetSceneObject() {
        synchronized (sceneObjectLock) {
            sceneObject = null;
        }
    }

    public GVRSceneObject getSceneObject() {
        synchronized (sceneObjectLock) {
            return sceneObject;
        }
    }

    /**
     * This is an important method with respect to the
     * {@link GVRCursorController}. In order to prevent excessive polling of the
     * transform data by the {@link GVRInputManager}, we leave it to the
     * {@link GVRCursorController} to let the {@link GVRInputManager} know when
     * the data is ready.
     * 
     * Make sure that a call to invalidate is made whenever the
     * {@link GVRCursorController} has new data to be processed.
     * 
     * @deprecated invalidate is now called internally whenever a new data is
     *             passed to the {@link GVRCursorController}. There is no need
     *             for an explicit {@link GVRCursorController#invalidate()}
     *             call.
     */
    protected void invalidate() {
    }

    /**
     * Return the {@link GVRCursorType} associated with the
     * {@link GVRCursorController}.
     * 
     * In most cases, this method should return {@link GVRCursorType#EXTERNAL}.
     * {@link GVRCursorType#EXTERNAL} allows the input device to define its own
     * input behavior. If the device wishes to implement
     * {@link GVRCursorType#MOUSE} or {@link GVRCursorType#CONTROLLER} make sure
     * that the behavior is consistent with that defined in
     * {@link GVRMouseDeviceManager} and {@link GVRGamepadDeviceManager}.
     * 
     * @return the {@link GVRCursorType} for the {@link GVRCursorController}.
     */
    public GVRCursorType getCursorType() {
        return cursorType;
    }

    /**
     * Set a key event. Note that this call can be used in lieu of
     * {@link GVRCursorController#setActive(boolean)}.
     * 
     * The {@link GVRCursorController} processes a {@link KeyEvent.ACTION_DOWN}
     * as active <code>true</code> and {@link KeyEvent.ACTION_UP} as active
     * <code>false</code>.
     * 
     * In addition the key event passed is used as a reference for applications
     * that wish to use the contents from the class.
     * 
     * {@link #setActive(boolean)} can still be used for applications that do
     * not want to expose key events.
     * 
     * @param keyEvent
     */
    protected void setKeyEvent(KeyEvent keyEvent) {
        synchronized (eventLock) {
            this.keyEvent = keyEvent;
        }
        if (keyEvent != null) {
            int action = keyEvent.getAction();
            if (action == KeyEvent.ACTION_DOWN && active == false) {
                setActive(true);
            } else if (action == KeyEvent.ACTION_UP && active == true) {
                setActive(false);
            }
        }
    }

    /**
     * Get the latest key event processed by the {@link GVRCursorController} if
     * there is one (not all {@link GVRCursorController} report {@link KeyEvent}
     * s). Note that this value will be null if the latest event processed by
     * the {@link GVRCursorController} did not contain a {@link KeyEvent}.
     * 
     * Note that this function also returns a null. To get every
     * {@link KeyEvent} reported by the {@link GVRCursorController} use the
     * {@link ControllerEventListener} or the {@link ISensorEvents} listener to
     * query for the {@link KeyEvent} whenever a a callback is made.
     * 
     * The {@link KeyEvent} would be valid for the lifetime of that callback and
     * would be reset to null on completion.
     * 
     * @return the {@link KeyEvent} or null if there isn't one.
     */
    public KeyEvent getKeyEvent() {
        synchronized (eventLock) {
            return processedKeyEvent;
        }
    }

    /**
     * Set the latest motion event processed by the {@link GVRCursorController}.
     * 
     * Make sure not to recycle the passed {@link MotionEvent}. The
     * {@link GVRCursorController} will recycle the {@link MotionEvent} after
     * completion.
     * 
     * @param motionEvent
     *            the {@link MotionEvent} processed by the
     *            {@link GVRCursorController}.
     */
    protected void setMotionEvent(MotionEvent motionEvent) {
        synchronized (eventLock) {
            if (this.motionEvent != null) {
                // its not yet been processed, recycle.
                this.motionEvent.recycle();
            }
            this.motionEvent = motionEvent;
        }

    }

    /**
     * Get the latest {@link MotionEvent} processed by the
     * {@link GVRCursorController} if there is one (not all
     * {@link GVRCursorController} report {@link MotionEvent}s)
     * 
     * Note that this function also returns a null. To get every
     * {@link MotionEvent} reported by the {@link GVRCursorController} use the
     * {@link ControllerEventListener} or the {@link ISensorEvents} listener to
     * query for the {@link MotionEvent} whenever a a callback is made.
     * 
     * The {@link MotionEvent} would be valid for the lifetime of that callback
     * and would be recycled and reset to null on completion. Make use to the
     * {@link MotionEvent#obtain(MotionEvent)} to clone a copy of the
     * {@link MotionEvent}.
     * 
     * @return the latest {@link MotionEvent} processed by the
     *         {@link GVRCursorController} or null.
     */
    public MotionEvent getMotionEvent() {
        synchronized (eventLock) {
            return processedMotionEvent;
        }
    }

    /**
     * This call sets the position of the {@link GVRCursorController}.
     * 
     * Use this call to also set an initial position for the Cursor when a new
     * {@link GVRCursorController} is returned by the
     * {@link CursorControllerListener}.
     * 
     * @param x
     *            the x value of the position.
     * @param y
     *            the y value of the position.
     * @param z
     *            the z value of the position.
     */
    public void setPosition(float x, float y, float z) {
        // check if the controller is enabled
        if (isEnabled() == false) {
            return;
        }

        position.set(x, y, z);
        if (sceneObject != null) {
            synchronized (sceneObjectLock) {
                // if there is an attached scene object then use its absolute
                // position.
                sceneObject.getTransform().setPosition(x, y, z);
            }
        }
        invalidate = true;
    }

    /**
     * Register a {@link ControllerEventListener} to receive a callback whenever
     * the {@link GVRCursorController} has been updated.
     * 
     * Use the {@link GVRCursorController} methods to query for information
     * about the {@link GVRCursorController}.
     */
    public static interface ControllerEventListener {
        public void onEvent(GVRCursorController controller);
    }

    /**
     * Add a {@link ControllerEventListener} to receive updates from this
     * {@link GVRCursorController}.
     * 
     * @param listener
     *            the {@link CursorControllerListener} to be added.
     */
    public void addControllerEventListener(ControllerEventListener listener) {
        controllerEventListeners.add(listener);
    }

    /**
     * Remove the previously added {@link ControllerEventListener}.
     * 
     * @param listener
     *            {@link ControllerEventListener} that was previously added .
     */
    public void removeControllerEventListener(
            ControllerEventListener listener) {
        controllerEventListeners.remove(listener);
    }

    /**
     * Use this method to enable or disable the {@link GVRCursorController}.
     * 
     * By default the {@link GVRCursorController} is enabled. If disabled, the
     * controller would not report new positions for the cursor and would not
     * generate {@link SensorEvent}s to {@link GVRBaseSensor}s.
     * 
     * @param enable
     *            <code>true</code> to enable the {@link GVRCursorController},
     *            <code>false</code> to disable.
     */
    public void setEnable(boolean enable) {
        this.enable = enable;

        if (enable == false) {
            // reset
            position.zero();
            ray.zero();
            if (previousActive) {
                active = false;
            }

            synchronized (eventLock) {
                keyEvent = null;
                if (motionEvent != null) {
                    motionEvent.recycle();
                    motionEvent = null;
                }
            }
            invalidate = true;
        }
    }

    /**
     * Check if the {@link GVRCursorController} is enabled or disabled.
     * 
     * By default the {@link GVRCursorController} is enabled.
     * 
     * @return <code>true</code> if the {@link GVRCursorController} is enabled,
     *         <code>false</code> otherwise.
     */
    public boolean isEnabled() {
        return enable;
    }

    /**
     * Set the near depth value for the controller. This is the closest the
     * {@link GVRCursorController} can get in relation to the {@link GVRCamera}.
     * 
     * By default this value is set to zero.
     * 
     * @param nearDepth
     */
    public void setNearDepth(float nearDepth) {
        this.nearDepth = nearDepth;
    }

    /**
     * Set the far depth value for the controller. This is the farthest the
     * {@link GVRCursorController} can get in relation to the {@link GVRCamera}.
     * 
     * By default this value is set to negative {@link Float#MAX_VALUE}.
     * 
     * @param farDepth
     */
    public void setFarDepth(float farDepth) {
        this.farDepth = farDepth;
    }

    /**
     * Get the near depth value for the controller.
     * 
     * @return value representing the near depth. By default the value returned
     *         is zero.
     */
    protected float getNearDepth() {
        return nearDepth;
    }

    /**
     * Get the far depth value for the controller.
     * 
     * @return value representing the far depth. By default the value returned
     *         is negative {@link Float#MAX_VALUE}.
     */
    protected float getFarDepth() {
        return farDepth;
    }

    /**
     * Process the input data
     */
    void update(SensorManager sensorManager, GVRScene scene) {
        if (invalidate) {

            // set the newly received key and motion events.
            synchronized (eventLock) {
                processedKeyEvent = keyEvent;
                keyEvent = null;
                processedMotionEvent = motionEvent;
                motionEvent = null;
            }

            if (previousActive == false && active) {
                activeState = ActiveState.ACTIVE_PRESSED;
            } else if (previousActive == true && active == false) {
                activeState = ActiveState.ACTIVE_RELEASED;
            } else {
                activeState = ActiveState.NONE;
            }

            previousActive = active;
            position.normalize(ray);
            invalidate = false;

            for (ControllerEventListener listener : controllerEventListeners) {
                listener.onEvent(this);
            }

            sensorManager.processPick(scene, this);

            // reset the set key and motion events.
            synchronized (eventLock) {
                processedKeyEvent = null;
                if (processedMotionEvent != null) {
                    // done processing, recycle
                    processedMotionEvent.recycle();
                    processedKeyEvent = null;
                }
            }
        }
    }

    Vector3f getRay() {
        return ray;
    }

}