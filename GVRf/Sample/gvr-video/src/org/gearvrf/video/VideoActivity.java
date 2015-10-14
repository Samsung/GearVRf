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

package org.gearvrf.video;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.util.VRTouchPadGestureDetector;
import org.gearvrf.util.VRTouchPadGestureDetector.OnTouchPadGestureListener;
import org.gearvrf.util.VRTouchPadGestureDetector.SwipeDirection;

public class VideoActivity extends GVRActivity implements
        OnTouchPadGestureListener {
    private static final int BUTTON_INTERVAL = 1000;
    private static final int TAP_INTERVAL = 300;
    private VideoScript mScript = null;
    private VRTouchPadGestureDetector mDetector = null;
    private BroadcastReceiver mBatteryReceiver = null;
    private long mLatestButton = 0;
    private long mLatestTap = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScript = new VideoScript(this);
        mDetector = new VRTouchPadGestureDetector(this);
        mBatteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctxt, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                if (mScript != null) {
                    mScript.setBatteryLevel(level);
                }
            }
        };
        setScript(mScript, "gvr_note4.xml");
        registerReceiver(mBatteryReceiver, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScript.onPause();
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() > mLatestButton + BUTTON_INTERVAL) {
            mLatestButton = System.currentTimeMillis();
            mScript.onButtonDown();
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mLatestButton = System.currentTimeMillis();
            mScript.onLongButtonPress();
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        mScript.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTap(MotionEvent e) {
        Log.v("", "onSingleTap");
        if (System.currentTimeMillis() > mLatestTap + TAP_INTERVAL) {
            mLatestTap = System.currentTimeMillis();
            mScript.onSingleTap(e);
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.v("", "onLongPress");
    }

    @Override
    public boolean onSwipe(MotionEvent e, SwipeDirection swipeDirection,
            float velocityX, float velocityY) {
        Log.v("", "onSwipe");
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return false;
    }
}
