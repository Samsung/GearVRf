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

package org.gearvrf.sample.sceneobjects;

import android.os.Bundle;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;

public class SceneObjectActivity extends GVRActivity {
    private static final String TAG = "SceneObjectActivity";
    private SampleViewManager mViewManager;
    private float lastY = 0;
    private float lastX = 0;
    private float lastYAngle = 0;
    private float lastXAngle = 0;
    private float yangle = 0;
    private float xangle = 0;
    private long lastDownTime = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewManager = new SampleViewManager();
        setScript(mViewManager, "gvr_note4.xml");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            lastY = event.getY();
            lastX = event.getX();
            lastDownTime = event.getDownTime();
        }

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            // check if it was a quick tap
            if (event.getEventTime() - lastDownTime < 200) {
                // if things are rotating, stop the rotation.
                if (xangle != 0 || yangle != 0) {
                    xangle = 0.0f;
                    yangle = 0.0f;
                    mViewManager.setXAngle(0.0f);
                    mViewManager.setYAngle(0.0f);
                    return true;
                }
                
                // otherwise, pass it as a tap to the ViewManager
                mViewManager.setXAngle(0.0f);
                mViewManager.setYAngle(0.0f);
                mViewManager.onTap();
            }

            float xdifference = lastX - event.getX();
            if (Math.abs(xdifference) > 10) {
                xangle = lastXAngle + xdifference / 10;
                mViewManager.setXAngle(1.0f);
                lastX = event.getX();
                lastXAngle = xangle;
            }

            float ydifference = lastY - event.getY();
            if (Math.abs(ydifference) > 10) {
                yangle = lastYAngle + ydifference / 10;
                mViewManager.setYAngle(1.0f);
                lastY = event.getY();
                lastYAngle = yangle;
            }
        }

        return true;
    }
}
