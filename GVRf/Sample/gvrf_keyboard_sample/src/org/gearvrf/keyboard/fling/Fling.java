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

package org.gearvrf.keyboard.fling;

import org.gearvrf.animation.GVRInterpolator;
import org.gearvrf.keyboard.interpolator.InterpolatorCubicEasyOut;
import org.gearvrf.keyboard.interpolator.InterpolatorExpoEaseOut;
import org.gearvrf.keyboard.interpolator.InterpolatorQuadEasyOut;
import org.gearvrf.keyboard.interpolator.InterpolatorQuartEasyOut;
import org.gearvrf.keyboard.interpolator.InterpolatorQuintEasyOut;

public class Fling {

    private static final float degreeRotation = 45f;

    private static final float timesAnimationRanger[] = new float[] {
            1.5f, 2.6f, 3f, 2.3f, 1.6f, 1f
    };

    // private static final float timesAnimationRanger [] = new float []{12f ,
    // 10f , 8f, 6f, 3f, 2f};
    // private static final float timesAnimationRanger [] = new float []{8f , 6f
    // , 4f, 3f, 3f, 2f};
    // private static final float timesAnimationRanger [] = new float []{6f , 4f
    // , 3f, 3f, 3f, 2f};
    // private static final float timesAnimationRanger [] = new float []{6f , 4f
    // , 3f, 3f, 3f, 2f};

    public static float getFactorMultiplier(float velocity) {

        // TODO change 13 to MAX rotation
        if (velocity >= 7000) {

            return 13;

        } else if (velocity >= 6000 && velocity < 7000) {

            return 8;

        } else if (velocity >= 5000 && velocity < 6000) {

            return 5;

        } else if (velocity >= 4000 && velocity < 5000) {

            return 3;

        } else if (velocity >= 3000 && velocity < 4000) {

            return 2;

        } else {

            return 1;
        }
    }

    public static float getDegreeRotation(float velocity) {

        // TODO change 13 to MAX rotation
        if (velocity >= 7000) {

            return 13 * degreeRotation;

        } else if (velocity >= 6000 && velocity < 7000) {

            return 8 * degreeRotation;

        } else if (velocity >= 5000 && velocity < 6000) {

            return 5 * degreeRotation;

        } else if (velocity >= 4000 && velocity < 5000) {

            return 3 * degreeRotation;

        } else if (velocity >= 3000 && velocity < 4000) {

            return 2 * degreeRotation;

        } else {

            return 1 * degreeRotation;
        }
    }

    public static float getDelayToAnimate(float velocity) {

        if (velocity >= 7000) {

            return timesAnimationRanger[0];

        } else if (velocity >= 6000 && velocity < 7000) {

            return timesAnimationRanger[1];

        } else if (velocity >= 5000 && velocity < 6000) {

            return timesAnimationRanger[2];

        } else if (velocity >= 4000 && velocity < 5000) {

            return timesAnimationRanger[3];

        } else if (velocity >= 3000 && velocity < 4000) {

            return timesAnimationRanger[4];

        } else {

            return timesAnimationRanger[5];
        }
    }

    public static GVRInterpolator getInterpolator(float velocity) {

        if (velocity >= 7000) {

            return new InterpolatorExpoEaseOut();

        } else if (velocity >= 6000 && velocity < 7000) {

            return new InterpolatorQuintEasyOut();

        } else if (velocity >= 5000 && velocity < 6000) {

            return new InterpolatorQuartEasyOut();

        } else if (velocity >= 4000 && velocity < 5000) {

            return new InterpolatorCubicEasyOut();

        } else if (velocity >= 3000 && velocity < 4000) {

            return new InterpolatorQuadEasyOut();

        } else {

            return new InterpolatorQuadEasyOut();
        }
    }
}
