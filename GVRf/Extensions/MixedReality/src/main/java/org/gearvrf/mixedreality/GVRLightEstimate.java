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

import org.gearvrf.GVRContext;

public abstract class GVRLightEstimate {

    protected GVRLightEstimate(GVRContext gvrContext) {  }

    public float getPixelIntensity() { return onGetPixelIntensity(); }

    public GVRLightEstimateState getLightEstimateState() { return onGetLightEstimateState(); }

    public enum GVRLightEstimateState {
        NOT_VALID(0),
        VALID(1);

        private int mCode;

        GVRLightEstimateState(int code) {
            this.mCode = code;
        }

        public int getCode() {
            return this.mCode;
        }
    }

    protected abstract GVRLightEstimateState onGetLightEstimateState();
    protected abstract float onGetPixelIntensity();
}
