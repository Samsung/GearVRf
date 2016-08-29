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

final class OvrConfigurationManager extends GVRConfigurationManager {

    OvrConfigurationManager(GVRActivity gvrActivity) {
        super(gvrActivity);
    }

    @Override
    public boolean isHmtConnected() {

        final GVRActivity activity = (GVRActivity) mActivity.get();
        if (null == activity) {
            return false;
        }
        return nativeIsHmtConnected(activity.getNative());
    }

    private static native boolean nativeIsHmtConnected(long ptr);
}
