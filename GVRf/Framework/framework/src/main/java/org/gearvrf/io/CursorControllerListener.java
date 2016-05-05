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

package org.gearvrf.io;

import org.gearvrf.GVRCursorController;

/**
 * Add a {@link CursorControllerListener} to the {@link GVRInputManager} to
 * receive notifications whenever a {@link GVRCursorController} is added or
 * removed from the system at runtime.
 * 
 */
public interface CursorControllerListener {

    /**
     * Called when a {@link GVRCursorController} is added to the system.
     * 
     * Use {@link GVRCursorController#getId()} to uniquely identify the
     * {@link GVRCursorController} and use
     * {@link GVRCursorController#getControllerType()} to know its
     * {@link GVRControllerType}.
     * 
     * @param controller
     *            the {@link GVRCursorController} added.
     */
    public void onCursorControllerAdded(GVRCursorController controller);

    /**
     * Called when the {@link GVRCursorController} previously added has been
     * removed.
     * 
     * Use {@link GVRCursorController#getId()} to uniquely identify the
     * {@link GVRCursorController} and
     * {@link GVRCursorController#getControllerType()} to know its
     * {@link GVRControllerType}.
     * 
     * @param controller
     *            the {@link GVRCursorController} removed.
     */
    public void onCursorControllerRemoved(GVRCursorController controller);

}