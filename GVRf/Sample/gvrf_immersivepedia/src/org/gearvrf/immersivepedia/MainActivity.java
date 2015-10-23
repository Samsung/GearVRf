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

package org.gearvrf.immersivepedia;

import android.os.Bundle;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.immersivepedia.input.TouchPadInput;
import org.gearvrf.immersivepedia.util.VRTouchPadGestureDetector;
import org.gearvrf.immersivepedia.util.VRTouchPadGestureDetector.OnTouchPadGestureListener;
import org.gearvrf.immersivepedia.util.VRTouchPadGestureDetector.SwipeDirection;

public class MainActivity extends GVRActivity implements OnTouchPadGestureListener {

    private MainScript mainScript;
    private VRTouchPadGestureDetector touchPadGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainScript = new MainScript();
        touchPadGestureDetector = new VRTouchPadGestureDetector(this);
        setScript(mainScript, "gvr_note4.xml");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchPadGestureDetector.onTouchEvent(event);
        TouchPadInput.input(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTap(MotionEvent e) {

        mainScript.onSingleTapConfirmed();
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onSwipe(MotionEvent e, SwipeDirection swipeDirection, float velocityX,
            float velocityY) {
        TouchPadInput.onSwipe(swipeDirection);
        mainScript.onSwipe();
        return false;
    }

}
