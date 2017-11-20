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
 * IO device types supported by the GVRf IO framework.
 */
public enum GVRControllerType {
    /**
     * This are the generic "mice" supported by Android. Both USB and Bluetooth "mice" are
     * handled by the IO framework.
     */
    MOUSE,
    /**
     * This type describes the gamepad controllers supported by Android. Description of which can
     * be found here:
     * https://developer.android.com/training/game-controllers/controller-input.html
     */
    GAMEPAD,
    /**
     * This type describes the head tracked gaze input method.
     */
    GAZE,
    /**
     * This type includes the 3 or 6 DOF orientation controllers that come with common VR
     * headsets. Examples include the gear controller or the daydream controller.
     * These input devices usually provide position, rotation, button and touch events to the
     * application.
     */
    CONTROLLER,
    /**
     * These are the input devices added externally to the framework using the
     * {@link GVRInputManager}.
     */
    EXTERNAL,
    /**
     * This controller type is returned when the device is not recognized by the framework.
     */
    UNKNOWN
}