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

package org.gearvrf.io;

import android.os.Bundle;

import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.stl.STLController;

public class InputActivity extends GVRActivity {
    private static final String TAG = InputActivity.class.getSimpleName();
    private InputScript script;
    private STLController controller;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        script = new InputScript();
        controller = new STLController(getApplicationContext(), script,
                new int[] { KeyEvent.KEYCODE_BUTTON_A,
                        KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_BUTTON_X,
                        KeyEvent.KEYCODE_BUTTON_Y, KeyEvent.KEYCODE_BUTTON_L1,
                        KeyEvent.KEYCODE_BUTTON_R1, KeyEvent.KEYCODE_BUTTON_L2,
                        KeyEvent.KEYCODE_BUTTON_R2, KeyEvent.KEYCODE_BUTTON_Y,
                        KeyEvent.KEYCODE_BUTTON_Z });
        setScript(script, "gvr_note4.xml");
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return controller.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        return controller.dispatchMotionEvent(event);
    }

    @Override
    protected void onDestroy() {
        controller.close();
        super.onDestroy();
    }

}
