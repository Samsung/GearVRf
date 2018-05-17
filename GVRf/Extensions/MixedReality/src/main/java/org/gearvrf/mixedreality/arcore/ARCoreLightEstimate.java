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

package org.gearvrf.mixedreality.arcore;

import org.gearvrf.mixedreality.GVRLightEstimate;


public class ARCoreLightEstimate extends GVRLightEstimate {
    /**
     * Set the pixel intensity of light
     *
     * @param intensity
     */
    protected void setPixelIntensity(float intensity) {
        mPixelIntensity = intensity;
    }

    /**
     * Set the state of light estimate
     *
     * @param state
     */
    protected void setState(GVRLightEstimateState state) {
        mState = state;
    }

    @Override
    public float getPixelIntensity() {
        return mPixelIntensity;
    }

    @Override
    public GVRLightEstimateState getLightEstimateState() {
        return mState;
    }
}
