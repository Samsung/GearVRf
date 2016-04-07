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

import android.content.Intent;
import android.content.res.Configuration;
import android.view.MotionEvent;

/**
 * This interface defines the callback interface of an Android {@code Activity}.
 * User can add a listener to {@code GVRActivity.getEventReceiver()} to handle
 * these events, rather than subclassing {@link GVRActivity}.
 */
public interface IActivityEvents extends IEvents {
    void onPause();

    void onResume();

    void onDestroy();

    void onSetScript(GVRScript script);

    void onWindowFocusChanged(boolean hasFocus);

    void onConfigurationChanged(Configuration config);

    void onActivityResult(int requestCode, int resultCode, Intent data);

    void onTouchEvent(MotionEvent event);

    void dispatchTouchEvent(MotionEvent event);
}
