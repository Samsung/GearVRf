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

import android.graphics.Bitmap;

/**
 * Callback interface for asynchronous screenshot capture. The screenshot result
 * will be passed to the caller as the parameter of
 * {@link GVRScreenshotCallback#onScreenCaptured(Bitmap) onScreenCaptured().}
 */
public interface GVRScreenshotCallback {
    /**
     * Callback method dealing with the returned screenshot
     * 
     * @param bitmap
     *            Bitmap containing the screenshot result
     */
    public void onScreenCaptured(Bitmap bitmap);
}
