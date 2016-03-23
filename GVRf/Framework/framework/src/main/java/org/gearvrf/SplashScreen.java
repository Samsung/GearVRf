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

package org.gearvrf;

import org.gearvrf.utility.Log;

class SplashScreen extends GVRSceneObject {

    private static final String TAG = Log.tag(SplashScreen.class);

    private boolean mCloseRequested;
    /**
     * Earliest time to close splash screen. Unit is nanos, as returned by
     * {@link GVRTime#getCurrentTime()}
     */
    final long mTimeout;

    SplashScreen(GVRContext gvrContext, GVRMesh mesh, GVRTexture texture,
            GVRMaterialShaderId shaderId, GVRScript script) {
        super(gvrContext, mesh, texture, shaderId);

        mCloseRequested = false; // unnecessary, but ...

        long currentTime = GVRTime.getCurrentTime();
        mTimeout = currentTime + (long) (script.getSplashDisplayTime() * 1e9f);

        Log.d(TAG, "currentTime = %,d, timeout = %,d", currentTime, mTimeout);
    }

    void closeSplashScreen() {
        mCloseRequested = true;
        Log.d(TAG, "close()");
    }

    boolean closeRequested() {
        return mCloseRequested;
    }

}
