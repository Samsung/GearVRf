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

import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.List;

/**
 * An instance of this class is obtained using the
 * {@link GVRContext#getInputManager()} call. Use this class to query for all
 * the {@link GVRCursorController} objects in the framework.
 *
 * Use the
 * {@link GVRInputManager#addCursorControllerListener(CursorControllerListener)}
 * call to notify the application whenever a {@link GVRCursorController} is
 * added or removed from the framework.
 *
 * Alternatively use the {@link GVRInputManager#getCursorControllers()} method
 * to know all the devices currently in the framework.
 *
 * The class also allows external input devices to be added using the
 * {@link GVRInputManager#addCursorController(GVRCursorController)} method.
 */
public interface GVRInputManager {

    /**
     * @todo
     */
    abstract public void scanControllers();

    /**
     * Get a list of the {@link GVRCursorController}s currently in the system.
     *
     * Ideally this call needs to be done inside
     * {@link org.gearvrf.GVRMain#onInit(GVRContext)} so that all the cursor objects are
     * set up before the rendering starts.
     *
     * Remember to add a {@link CursorControllerListener} to receive
     * notifications on {@link GVRCursorController} objects added or removed
     * during runtime.
     *
     * @return a list of all the {@link GVRCursorController} objects in the
     * system.
     */
    List<GVRCursorController> getCursorControllers();

    /**
     * Get the first controller of a specified type
     * @param type controller type to search for
     * @return controller found or null if no controllers of the given type
     */
    GVRCursorController findCursorController(GVRControllerType type);

    /**
     * @todo
     * @return
     */
    GearCursorController getGearController();

    /**
     * Queries the status of the connection to the Android wear watch.
     * @see IWearTouchpadEvents
     * @return true if android wear touchpad is connected, else false.
     */
    boolean isConnectedToAndroidWearTouchpad();

    /**
     * Dispatch a {@link KeyEvent} to the {@link GVRInputManager}.
     *
     * @param event The {@link KeyEvent} to be processed.
     * @return <code>true</code> if the {@link KeyEvent} is handled by the
     * {@link GVRInputManager}, <code>false</code> otherwise.
     */
    boolean dispatchKeyEvent(KeyEvent event);

    /**
     * Dispatch a {@link MotionEvent} to the {@link GVRInputManager}.
     *
     * @param event The {@link MotionEvent} to be processed.
     * @return <code>true</code> if the {@link MotionEvent} is handled by the
     * {@link GVRInputManager}, <code>false</code> otherwise.
     */
    boolean dispatchMotionEvent(MotionEvent event);

    /**
     * Add a {@link CursorControllerListener} to receive an event whenever a
     * {@link GVRCursorController} is added or removed from the framework.
     *
     * @param listener the {@link CursorControllerListener} to be added.
     */
    void addCursorControllerListener(CursorControllerListener listener);

    /**
     * Remove the previously added {@link CursorControllerListener}.
     *
     * @param listener the {@link CursorControllerListener} to be removed.
     */
    void removeCursorControllerListener(CursorControllerListener listener);

    /**
     * Define a {@link GVRCursorController} and add it to the
     * {@link GVRInputManager} for external input device handling by the
     * framework.
     *
     * @param controller the external {@link GVRCursorController} to be added to the
     *                   framework.
     */
    void addCursorController(GVRCursorController controller);

    /**
     * Remove the previously added {@link GVRCursorController} added to the
     * framework.
     *
     * @param controller the external {@link GVRCursorController} to be removed from
     *                   the framework.
     */
    void removeCursorController(GVRCursorController controller);

    /**
     * Signal the previously added {@link GVRCursorController} is now
     * active and generating events.
     * @param controller the external {@link GVRCursorController} that is active
     */
    void activateCursorController(GVRCursorController controller);

    /**
     * Signal the previously added {@link GVRCursorController} is no longer
     * active and is not generating events.
     * @param controller the external {@link GVRCursorController} that is inactive
     */
    void deactivateCursorController(GVRCursorController controller);
}
