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

/**
 * This class feeds input to the {@link GVRInputManager}.
 * 
 * Future action items:<br>
 * Support timestamps?
 * 
 */
public class InputEvent {
    private float x, y, z;
    private int pointerId;
    private boolean active;

    // We take a leaf out of the MotionEvent book to implement the linked
    // recycling of objects.
    private static final int MAX_RECYCLED = 10;
    private static final Object recyclerLock = new Object();
    private static int recyclerUsed;
    private static InputEvent recyclerTop;
    private InputEvent next;

    /**
     * Use this call to retrieve an object of {@link InputEvent}.
     * 
     * @return An {@link InputEvent} to be used.
     */
    public static InputEvent obtain() {
        final InputEvent event;
        synchronized (recyclerLock) {
            event = recyclerTop;
            if (event == null) {
                return new InputEvent();
            }
            recyclerTop = event.next;
            recyclerUsed -= 1;
        }
        event.next = null;
        return event;
    }

    public final void recycle() {

        synchronized (recyclerLock) {
            if (recyclerUsed < MAX_RECYCLED) {
                recyclerUsed++;
                next = recyclerTop;
                recyclerTop = this;
            }
        }
    }

    /**
     * 
     * Get the X axis value for the {@link InputEvent}.
     * 
     * @return The value of the X axis.
     * 
     */
    public float getX() {
        return x;
    }

    /**
     * 
     * Get the Y axis value for the {@link InputEvent}.
     * 
     * @return The value of the Y axis.
     * 
     */
    public float getY() {
        return y;
    }

    /**
     * 
     * Get the X axis value for the {@link InputEvent}.
     * 
     * @return The value of the X axis.
     * 
     */
    public float getZ() {
        return z;
    }

    /**
     * Get the active state of the {@link InputEvent}.
     * 
     * @return The active flag.<br>
     *         For more information on this flag look at
     *         {@link InputEvent#setActive(boolean)}
     */
    public boolean getIsActive() {
        return active;
    }

    /**
     * Get the pointer that the {@link InputEvent} is mean't for.
     * 
     * @return The id of the pointer the {@link InputEvent} corresponds to.
     */
    public int getPointerId() {
        return pointerId;
    }

    /**
     * Set the X axis value for the {@link InputEvent}.
     * 
     * @param x
     *            The value of the X axis.
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * Set the Y axis value for the {@link InputEvent}.
     * 
     * @param y
     *            The value of the Y axis.
     */
    public void setY(float y) {
        this.y = y;
    }

    /**
     * Set the Z axis value for the {@link InputEvent}.
     * 
     * @param z
     *            The value of the Z axis.
     */
    public void setZ(float z) {
        this.z = z;
    }

    /**
     * Specify the pointer that this {@link InputEvent} is meant for.
     * 
     * @param pointerId
     *            The pointer affected by the data contained in this object.
     */
    public void setPointerId(int pointerId) {
        this.pointerId = pointerId;
    }

    /**
     * Use this method to set the active state of the {@link InputEvent}.
     * 
     * @param active
     *            This flag is usually attached to a button press, it is up to
     *            the creator to map the designated button to the active flag.
     * 
     *            Eg. A Gamepad could attach {@link KeyEvent#KEYCODE_BUTTON_A}
     *            to active. Setting active to true would result in a
     *            {@link SensorEvent} with {@link SensorEvent#isActive()} as
     *            <code>true</code> the affected that {@link GVRSceneObject}.
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
