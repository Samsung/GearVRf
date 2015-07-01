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

package org.gearvrf.gamepad;

import org.gearvrf.GVRActivity;
import org.gearvrf.utility.Log;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.InputDevice;

public class SampleActivity extends GVRActivity {

    private SampleViewManager mScript = null;
    private long lastClickMillis;
    private static final long THRESHOLD_MILLIS = 500L;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mScript = new SampleViewManager();
        setScript(mScript, "gvr_note4.xml");
    }

    @Override
    public boolean dispatchKeyEvent(android.view.KeyEvent event) {

        long now = SystemClock.elapsedRealtime();
        if (now - lastClickMillis > THRESHOLD_MILLIS) {
            lastClickMillis = now;
            mScript.processKeyEvent(event.getKeyCode());
        }
        return true;
    }

    @Override
    public boolean dispatchGenericMotionEvent(android.view.MotionEvent event) {

        if (event.getAction() == android.view.MotionEvent.ACTION_MOVE) {
            mScript.processMotionEvent(
                    event.getAxisValue(android.view.MotionEvent.AXIS_HAT_X),
                    event.getAxisValue(android.view.MotionEvent.AXIS_HAT_Y));
        }

        return true;
    }

}
