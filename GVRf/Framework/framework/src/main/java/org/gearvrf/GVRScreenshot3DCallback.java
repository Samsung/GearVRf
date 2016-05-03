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

import java.util.List;

import android.graphics.Bitmap;

/**
 * Callback interface for asynchronous 3D screenshot capture. The screenshot
 * result will be passed to the caller as the parameter of
 * {@link GVRScreenshot3DCallback#onScreenCaptured(List)}.
 */
public interface GVRScreenshot3DCallback {
    /**
     * Callback method dealing with the returned screenshots
     * 
     * @param bitmapArray
     *            Array containing six screenshot results. The six images are for
     *            +x, -x, +y, -y, +z, -z directions respectively.
     */
    public void onScreenCaptured(Bitmap[] bitmapArray);
}