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
 * An interpolator that initially accelerates; runs at fairly steady speed; then
 * finally decelerates.
 */
public class GVRAccelerateDecelerateInterpolator implements GVRInterpolator {

    private static GVRAccelerateDecelerateInterpolator sInstance = null;

    private GVRAccelerateDecelerateInterpolator() {
    }

    /** Get the (lazy-created) singleton */
    public static synchronized GVRAccelerateDecelerateInterpolator getInstance() {
        if (sInstance == null) {
            sInstance = new GVRAccelerateDecelerateInterpolator();
        }
        return sInstance;
    }

    @Override
    public float mapRatio(float ratio) {
        float floor = (float) Math.floor(ratio);
        float residual = ratio - floor;
        return floor + (-0.5f * ((float) Math.cos(residual * Math.PI) - 1.0f));
    }

}
