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

import android.opengl.Matrix;

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
    private static int uniqueControllerId = 0;
    private final int controllerId;
    private final GVRCursorType cursorType;
    private boolean previousActive;
    private ActiveState activeState = ActiveState.NONE;
    private boolean invalidate = false;
    private boolean active;
    private float rayX, rayY, rayZ;
    private float nearDepth, farDepth = -Float.MAX_VALUE;
    private float[] position;

    private GVRSceneObject sceneObject;
    private Object sceneObjectLock = new Object();

    public GVRCursorController(GVRCursorType cursorType) {
        this.controllerId = uniqueControllerId;
        this.cursorType = cursorType;
        uniqueControllerId++;
        position = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
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
        this.active = active;
        invalidate = true;
    }

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
                object.getTransform().setPosition(position[0], position[1],
                        position[2]);
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
     * {@link GVRCursorController} to let the {@link GVRInputManager} whe
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
        position[0] = x;
        position[1] = y;
        position[2] = z;
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

            float inverseLength = (float) (1 / (Math.sqrt(square(position[0])
                    + square(position[1]) + square(position[2]))));

            rayX = position[0] * inverseLength;
            rayY = position[1] * inverseLength;
            rayZ = position[2] * inverseLength;

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