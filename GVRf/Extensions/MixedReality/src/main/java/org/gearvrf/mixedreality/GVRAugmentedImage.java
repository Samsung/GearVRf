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

package org.gearvrf.mixedreality;

/**
 * Represents a common Augmented Image in MixedReality
 */
public abstract class GVRAugmentedImage {
    protected GVRTrackingState mTrackingState;

    /**
     * @return Returns the estimated width
     */
    public abstract float getExtentX();

    /**
     * @return Returns the estimated height
     */
    public abstract float getExtentZ();

    /**
     *
     * @return The augmented image center pose
     */
    public abstract float[] getCenterPose();

    /**
     *
     * @return The tracking state
     */
    public abstract  GVRTrackingState getTrackingState();
}
