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

import com.gearvrf.stl.STLController;

public class ControllerActivity extends GVRActivity {
    private ControllerScript script;
    private STLController controller;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        script = new ControllerScript();
        controller = new STLController(script);
        setScript(script, "gvr_note4.xml");
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        controller.dispatchKeyEvent(event);
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        controller.dispatchMotionEvent(event);
        return super.dispatchGenericMotionEvent(event);
    }

    @Override
    protected void onDestroy() {
        controller.close();
        super.onDestroy();
    }
}
