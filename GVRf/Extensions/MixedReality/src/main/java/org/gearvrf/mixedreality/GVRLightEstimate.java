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
 * Represents the light estimate of a frame in scene
 */
public abstract class GVRLightEstimate {
    protected float mPixelIntensity;
    protected GVRLightEstimateState mState;

    /**
     *
     * @return The pixel intensity of light
     */
    public abstract float getPixelIntensity();

    /**
     *
     * @return The state of light estimate
     */
    public abstract GVRLightEstimateState getLightEstimateState();

    /**
     * Describes the two possible states of a light estimative.
     */
    public enum GVRLightEstimateState {
        NOT_VALID,
        VALID
    }
}
