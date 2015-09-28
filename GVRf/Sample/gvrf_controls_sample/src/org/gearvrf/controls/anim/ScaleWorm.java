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

package org.gearvrf.controls.anim;

import org.gearvrf.controls.MainScript;
import org.gearvrf.controls.Worm;

public class ScaleWorm {

    public static boolean animPlaying;

    private static float[] lastSize = new float[] {
            0, 0, 0
    };

    private static boolean animPlayed = true;

    private static Worm mWorm;

    public static void putWormPreviewReference(Worm mWorm) {
        ScaleWorm.mWorm = mWorm;
    }

    public static synchronized void setLastSize() {

        if (animPlayed) {

            animPlayed = false;

            ScaleWorm.lastSize[0] = mWorm.getHead().getTransform().getScaleX();
            ScaleWorm.lastSize[1] = mWorm.getMiddle().getTransform().getScaleX();
            ScaleWorm.lastSize[2] = mWorm.getEnd().getTransform().getScaleX();

            MainScript.animWormReset();
        }
    }

    public static float[] getLastSize() {
        return lastSize;
    }

    public static Worm getWorm() {
        return mWorm;
    }

    public static void playAnim() {
        animPlayed = true;
    }

    public static boolean scaleAnimIsEnable() {
        if (lastSize[0] != 0) {
            return true;
        } else {
            return false;
        }
    }
}
