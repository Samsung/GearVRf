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

/**
 * Listener for camera update events.
 */
public interface ICameraEvents extends IEvents
{
    /**
     * Called whenever the camera view matrix is changed by the headset.
     * The camera rig is already updated when this function is invoked.
     * It is called on the GL thread right BEFORE rendering so do not
     * do a lot of work in this handler!
     * @param rig camera rig that was updated
     */
    public void onViewChange(GVRCameraRig rig);
}
