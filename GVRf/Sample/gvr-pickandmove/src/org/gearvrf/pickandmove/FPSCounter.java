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


package org.gearvrf.pickandmove;

import android.util.Log;

public class FPSCounter {
    private static int frames = 0;
    private static long startTimeMillis = 0;
    private static final long interval = 10000;

    public static void tick() {
        ++frames;
        if (System.currentTimeMillis() - startTimeMillis >= interval) {
            Log.v("", "FPS : " + frames / (interval / 1000.0f));
            frames = 0;
            startTimeMillis = System.currentTimeMillis();
        }
    }
}
