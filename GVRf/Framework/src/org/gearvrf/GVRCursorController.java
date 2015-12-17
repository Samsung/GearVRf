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

import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRCursorType;
import org.gearvrf.io.GVRInputManager;

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
 * Use the {@link GVRInputManager#addCursorController(GVRCursorController)}
 * call to add an external {@link GVRCursorController} to the framework.
 * 
 */
public abstract class GVRCursorController {
    private static int uniqueControllerId = 0;
    private final int controllerId;
    private final GVRCursorType cursorType;
    private boolean previousActive;
    private ActiveState activeState = ActiveState.NONE;
    private boolean invalidate = false;
    private boolean active;
    private float rayX, rayY, rayZ;
    private float x = 0.0f, y = 0.0f, z = -7.0f;
    private GVRSceneObject sceneObject;
    private Object sceneObjectLock = new Object();

    public GVRCursorController(GVRCursorType cursorType) {
        this.controllerId = uniqueControllerId;
        this.cursorType = cursorType;
        uniqueControllerId++;
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
        this.active = active;
    }

    public void setSceneObject(GVRSceneObject object) {
        synchronized (sceneObjectLock) {
            sceneObject = object;
            sceneObject.getTransform().setPosition(x, y, z);
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
     * {@link GVRCursorController}.
     * 
     * In order to prevent excessive polling of the transform data by the
     * {@link GVRInputManager}, we leave it to the {@link GVRCursorController}
     * to let the {@link GVRInputManager} when to data is ready.
     * 
     * Make sure that a call to invalidate is made whenever the
     * {@link GVRCursorController} has new data to be processed.
     */
    protected void invalidate() {
        invalidate = true;
    }

    /**
     * Get the {@link GVRCursorType} of this object.
     * 
     * @return the {@link GVRCursorType} that this object represents.
     */
    public GVRCursorType getCursorType() {
        return cursorType;
    }

    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        if (sceneObject != null) {
            synchronized (sceneObjectLock) {
                sceneObject.getTransform().setPosition(x, y, z);
            }
        }
    }

    /**
     * Process the input data and return true if changed.
     * 
     * @return
     */
    boolean update() {
        if (invalidate) {
            if (previousActive == false && active) {
                activeState = ActiveState.ACTIVE_PRESSED;
            } else if (previousActive == true && active == false) {
                activeState = ActiveState.ACTIVE_RELEASED;
            } else {
                activeState = ActiveState.NONE;
            }

            previousActive = active;
            float inverseLength = (float) (1
                    / (Math.sqrt(square(x) + square(y) + square(z))));
            rayX = x * inverseLength;
            rayY = y * inverseLength;
            rayZ = z * inverseLength;

            invalidate = false;
            return true;
        }
        return false;
    }

    private static float square(float x) {
        return x * x;
    }

    float getRayX() {
        return rayX;
    }

    float getRayY() {
        return rayY;
    }

    float getRayZ() {
        return rayZ;
    }
}