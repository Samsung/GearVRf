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


package org.gearvrf.sixaxissensortest;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.sixaxissensortest.R;

public class TestActivity extends GVRActivity implements OnGestureListener, OnDoubleTapListener {


    private enum Parameter{A, B};
    private TestViewManager mViewManager = null;
    private GestureDetector mDetector = null;
    private Parameter mParameter = Parameter.A;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mViewManager = new TestViewManager();
        setScript(mViewManager, "gvr_note4.xml");
        mDetector = new GestureDetector(this, this);
        mDetector.setOnDoubleTapListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return mDetector.onTouchEvent(e);
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        mViewManager.onDoubleTap();
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId() ) {
        case R.id.item1:
            mParameter = Parameter.A;
            break;
        case R.id.item2:
            mParameter = Parameter.B;
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                volumeUp();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                volumeDown();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void volumeUp()
    {
        if (mParameter == Parameter.A)
        {
            mViewManager.addAValue(1.0f);
        }
        else
        {
            mViewManager.addBValue(1.0f);
        }
    }

    private void volumeDown()
    {
        if (mParameter == Parameter.A)
        {
            mViewManager.addAValue(-1.0f);
        }
        else
        {
            mViewManager.addBValue(-1.0f);
        }
    }
}
