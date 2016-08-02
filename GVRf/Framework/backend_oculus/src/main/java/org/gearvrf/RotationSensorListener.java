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
 * Notification interface for {@link RotationSensor rotation sensor} data
 * events.
 */
interface RotationSensorListener {
    /**
     * Called when the active rotation sensor has generated data. Orientation is
     * passed as components of a quaternion. The angular velocity of the device
     * is passed separately for each axis; when the internal rotation sensor is
     * being used, these will always be {@code 0.0f}.
     * 
     * @param timeStamp
     *            Clock-time when the data was received, in nanoseconds.
     * @param w
     *            The 'W' rotation component.
     * @param x
     *            The 'X' rotation component.
     * @param y
     *            The 'Y' rotation component.
     * @param z
     *            The 'Z' rotation component.
     * @param gyroX
     *            Angular velocity on the 'X' axis.
     * @param gyroY
     *            Angular velocity on the 'Y' axis.
     * @param gyroZ
     *            Angular velocity on the 'Z' axis.
     */
    void onRotationSensor(long timeStamp, float w, float x, float y, float z,
            float gyroX, float gyroY, float gyroZ);
}
