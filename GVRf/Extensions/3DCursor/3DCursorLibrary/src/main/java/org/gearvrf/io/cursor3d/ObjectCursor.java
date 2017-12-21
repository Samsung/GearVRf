/*
 * Copyright 2016 Samsung Electronics Co., LTD
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

package org.gearvrf.io.cursor3d;

import android.util.Log;
import android.view.MotionEvent;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.ITouchEvents;
import org.gearvrf.io.cursor3d.CursorAsset.Action;

import java.util.HashSet;
import java.util.Set;

class ObjectCursor extends Cursor {
    private static final String TAG = ObjectCursor.class.getSimpleName();
    private static final float POINT_CURSOR_NEAR_DEPTH = 3.0f;
    private int mColliderID = -1;

    ObjectCursor(GVRContext context, CursorManager cursorManager) {
        super(context, CursorType.OBJECT, cursorManager);

        Log.d(TAG, Integer.toHexString(hashCode()) + " constructed");
        mTouchListener = new ITouchEvents()
        {
            public void onEnter(GVRSceneObject obj, GVRPicker.GVRPickedObject hit)
            {
                checkAndSetAsset(Action.INTERSECT);
            }

            public void onExit(GVRSceneObject obj, GVRPicker.GVRPickedObject hit)
            {
                checkAndSetAsset(Action.DEFAULT);
            }

            public void onInside(GVRSceneObject obj, GVRPicker.GVRPickedObject hit) { }

            public void onTouchStart(GVRSceneObject obj, GVRPicker.GVRPickedObject hit)
            {
                checkAndSetAsset(Action.CLICK);
            }

            public void onTouchEnd(GVRSceneObject obj, GVRPicker.GVRPickedObject hit)
            {
                checkAndSetAsset(Action.DEFAULT);
            }

            public void onMotionOutside(GVRPicker picker, MotionEvent event) { }
        };
    }

    public int getColliderID() { return mColliderID; }

    public void setColliderID(int id) { mColliderID = id; }


    @Override
    void setCursorDepth(float depth) {
        if (depth > MAX_CURSOR_SCALE) {
            return;
        }

        // place the cursor at half the depth scale
        super.setCursorDepth(depth / 2);

        IoDevice device = getIoDevice();
        if (device != null) {
            device.setNearDepth(POINT_CURSOR_NEAR_DEPTH);
        }
    }

    @Override
    void setIoDevice(IoDevice ioDevice) {
        super.setIoDevice(ioDevice);
        ioDevice.setNearDepth(POINT_CURSOR_NEAR_DEPTH);
    }

    @Override
    void setupIoDevice(IoDevice ioDevice) {
        super.setupIoDevice(ioDevice);
        ioDevice.setDisableRotation(false);
    }
}