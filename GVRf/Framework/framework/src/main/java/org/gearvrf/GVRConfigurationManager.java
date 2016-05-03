/* Copyright 2016 Samsung Electronics Co., LTD
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

import java.lang.ref.WeakReference;

final class GVRConfigurationManager {

    private WeakReference<GVRActivity> mActivity;
    private static GVRConfigurationManager sInstance;

    private GVRConfigurationManager(GVRActivity gvrActivity) {
        mActivity = new WeakReference<GVRActivity>(gvrActivity);
    }

    static void onInitialize(GVRActivity activity) {
        sInstance = new GVRConfigurationManager(activity);
    }

    /**
     * Get the instance of this class
     * 
     * @return the Singleton instance of this class.
     * */
    public static GVRConfigurationManager getInstance() {
        return sInstance;
    }

    /**
     * @return true if GearVR is connected, false otherwise
     */
    public boolean isHmtConnected() {
        final GVRActivity activity = mActivity.get();
        if (null == activity) {
            return false;
        }

        return nativeIsHmtConnected(activity.getNative());
    }

    private static native boolean nativeIsHmtConnected(long ptr);
}
