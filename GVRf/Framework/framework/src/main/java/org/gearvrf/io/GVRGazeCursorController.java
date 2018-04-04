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

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.joml.Vector3f;

import java.util.concurrent.CountDownLatch;

final public class GVRGazeCursorController extends GVRCursorController
{
    private static final float DEPTH_SENSITIVITY = 0.1f;
    private float actionDownX;
    private float actionDownZ;
    private final KeyEvent BUTTON_GAZE_DOWN = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BUTTON_1);
    private final KeyEvent BUTTON_GAZE_UP = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BUTTON_1);

    GVRGazeCursorController(GVRContext context,
                            GVRControllerType controllerType,
                            String name, int vendorId, int productId)
    {
        super(context, controllerType, name, vendorId, productId);
        mConnected = true;
    }

    @Override
    synchronized public boolean dispatchKeyEvent(KeyEvent event) {
        if (isEnabled())
        {
            setKeyEvent(event);
            return true;
        }
        return false;
    }

    @Override
    synchronized public boolean dispatchMotionEvent(MotionEvent event)
    {
        if (isEnabled())
        {
            handleMotionEvent(MotionEvent.obtain(event));
            return true;
        }
        return false;
    }

    private void handleMotionEvent(MotionEvent event)
    {
        float eventX = event.getX();
        int action = event.getAction();
        float deltaX;
        int button = event.getButtonState();

        if (button == 0)
        {
            button = MotionEvent.BUTTON_PRIMARY;
        }
        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
            actionDownX = eventX;
            actionDownZ = mCursorDepth;
            if ((mTouchButtons & button) != 0)
            {
                setActive(true);
            }
            // report ACTION_DOWN as a button
            setKeyEvent(BUTTON_GAZE_DOWN);
            break;

            case MotionEvent.ACTION_UP:
            setActive(false);
            setKeyEvent(BUTTON_GAZE_UP);
            break;

            case MotionEvent.ACTION_MOVE:
            deltaX = eventX - actionDownX;
            if (mCursorControl == CursorControl.CURSOR_DEPTH_FROM_CONTROLLER)
            {
                float eventZ = actionDownZ + deltaX * DEPTH_SENSITIVITY;
                if (eventZ <= getNearDepth())
                {
                    eventZ = getNearDepth();
                }
                if (eventZ >= getFarDepth())
                {
                    eventZ = getFarDepth();
                }
                setCursorDepth(eventZ);
            }
            break;
            default:
                event.recycle();
                return;
        }
        setMotionEvent(event);
        invalidate();
    }
}
