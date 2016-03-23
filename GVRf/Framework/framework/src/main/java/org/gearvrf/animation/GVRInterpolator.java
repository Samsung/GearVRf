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

package org.gearvrf.animation;

/**
 * Allows you to override the normal, linear animation.
 * 
 * That is, animations proceed linearly by default: at 75% of the duration, the
 * animated property will be 75% of the way from the start state to the stop
 * state. An interpolator lets you override that, so the animation can
 * accelerate and/or decelerate; overshoot; bounce; and so on.
 */
public interface GVRInterpolator {
    /**
     * Map from "time ratio" to "property change ratio." How you do this mapping
     * is totally up to you: some animations can handle mapped ratios
     * {@literal <} 0 or {@literal >} 1, but others can not.
     * 
     * @param ratio
     *            the current time ratio, {@literal >=} 0 and {@literal <=} 1
     * @return a value no less than 0 and no greater than 1.
     */
    float mapRatio(float ratio);
}
