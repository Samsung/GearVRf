/* Copyright 2016 Samsung Electronics Co., LTD
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

import org.gearvrf.scene_objects.GVRKeyboardSceneObject;

/**
 * Interface definition for a callback to be invoked when a key event is dispatched
 * to a {@link GVRSceneObject}.
 */
public interface IKeyboardEvents extends IEvents {
    /**
     * Send a key press to the listener.
     * @param sceneObject {@linkplain GVRKeyboardSceneObject view scene object}
     * @param primaryCode this is the key that was pressed
     * @param keyCodes the codes for all the possible alternative keys
     * with the primary code being the first. If the primary key code is
     * a single character such as an alphabet or number or symbol, the alternatives
     * will include other characters that may be on the same key or adjacent keys.
     * These codes are useful to correct for accidental presses of a key adjacent to
     * the intended key.
     */
    void onKey(GVRKeyboardSceneObject sceneObject, int primaryCode, int[] keyCodes);
    void onStartInput(GVRKeyboardSceneObject sceneObject);
    void onStopInput(GVRKeyboardSceneObject sceneObject);
}
