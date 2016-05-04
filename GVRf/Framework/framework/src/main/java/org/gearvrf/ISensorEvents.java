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
 * Use this listener to receive {@link SensorEvent}s for a given
 * {@link GVRBaseSensor}.
 * 
 * Note that the callbacks happen on the main UI thread.
 *
 */
public interface ISensorEvents extends IEvents {
    /**
     * The callback that returns the {@link SensorEvent}s for the corresponding
     * {@link GVRBaseSensor}.
     * 
     * @param event
     *            The {@link ISensorEvents} returns one
     *            {@link SensorEvent} for every affected object. Use the methods
     *            in {@link SensorEvent} to retrieve the information contained
     *            in the event.
     */
    void onSensorEvent(SensorEvent event);
}
