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

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.GVRBaseSensor;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRScene;
import org.gearvrf.IActivityEvents;
import org.gearvrf.utility.Log;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.concurrent.CountDownLatch;
import static java.lang.Float.max;

final public class GVRGazeCursorController extends GVRCursorController
{
    private static final int TAP_TIMEOUT = 60;
    private static float TOUCH_SQUARE = 8.0f * 8.0f;
    private static final float DEPTH_SENSITIVITY = 0.1f;
    private int referenceCount;
    private float actionDownX;
    private float actionDownY;
    private float actionDownZ;

    // Used to calculate the gaze-direction
    private final Object lock = new Object();
    private EventHandlerThread thread;

    GVRGazeCursorController(GVRContext context,
                            GVRControllerType controllerType, String name, int vendorId,
                            int productId) {
        super(context, controllerType, name, vendorId, productId);
        this.context = context;
        thread = new EventHandlerThread();
        mConnected = true;
    }

    /*
     * The increment the reference count to let the cursor controller know how
     * many input devices are using this controller.
     */
    void incrementReferenceCount() {
        referenceCount++;
        if (referenceCount == 1 && isEnabled()) {
            start();
        }
    }

    private void start(){
        if (!thread.isAlive()) {
            thread.start();
            thread.await();
        }
    }

    private void stop()
    {
    }

    /**
     * The decrement the reference count to let the cursor controller know how
     * many input devices are using this controller.
     *
     * @return returns <code>true</code> when the count hits zero, <code>false</code> otherwise.
     */
    boolean decrementReferenceCount() {
        referenceCount--;
        // no more devices
        if (referenceCount == 0 && isEnabled()) {
            stop();
            return true;
        }

        return false;
    }

    @Override
    public synchronized void setScene(GVRScene scene) {
        mPicker.setScene(scene);
        if (!thread.isAlive()) {
            super.setScene(scene);
        } else {
            thread.setScene(scene);
        }
    }

    @Override
    public synchronized void invalidate() {
        if (thread.isAlive()) {
            thread.sendInvalidate();
        }
    }

    @Override
    synchronized public boolean dispatchKeyEvent(KeyEvent event) {
        if (thread.isAlive())
        {
            thread.dispatchKeyEvent(event);
            return !mSendEventsToActivity;
        }
        return false;
    }

    @Override
    synchronized public boolean dispatchMotionEvent(MotionEvent event)
    {
        if (thread.isAlive())
        {
            thread.dispatchMotionEvent(event);
            return !mSendEventsToActivity;
        }
        return false;
    }

    @Override

    public synchronized void setEnable(boolean flag) {
        boolean enabled = isEnabled();
        super.setEnable(flag);
        if (!enabled && flag) {
            if (referenceCount > 0) {
                start();
                thread.setEnabled(true);
            }
        } else if (enabled && !flag) {
            if (referenceCount > 0) {
                thread.setEnabled(false);
                stop();
            }
        }
    }

    @Override
    public void setPosition(float x, float y, float z)
    {
        thread.setPosition(x, y, z);
    }

    void close() {
        if (referenceCount > 0) {
            referenceCount = 0;
        }
        if (thread.isAlive()) {
            thread.quitSafely();
        }
    }

    private final class EventHandlerThread extends HandlerThread {
        private static final String THREAD_NAME = "GVRGazeEventHandlerThread";
        public static final int SET_POSITION = 0;
        public static final int SET_KEY_EVENT = 1;
        public static final int SET_ENABLE = 4;
        public static final int SET_SCENE = 5;
        public static final int SEND_INVALIDATE = 6;
        public static final int SET_MOTION_EVENT = 7;

        public static final int ENABLE = 0;
        public static final int DISABLE = 1;

        private final KeyEvent BUTTON_GAZE_DOWN = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BUTTON_1);
        private final KeyEvent BUTTON_GAZE_UP = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BUTTON_1);
        private Handler gazeEventHandler;
        private final Vector3f position;
        private CountDownLatch mRunning = new CountDownLatch(1);

        public EventHandlerThread() {
            super(THREAD_NAME);
            position = new Vector3f();
        }

        @Override
        protected void onLooperPrepared() {
            gazeEventHandler = new Handler(getLooper(), new Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    switch (msg.what) {
                        case SET_POSITION:
                            float x, y, z;
                            synchronized (position) {
                                x = position.x;
                                y = position.y;
                                z = position.z;
                            }
                            GVRGazeCursorController.super.setPosition(x, y, z);
                            break;
                        case SET_MOTION_EVENT:
                            MotionEvent motionEvent = (MotionEvent) msg.obj;
                            handleMotionEvent(motionEvent);
                            break;
                        case SET_KEY_EVENT:
                            KeyEvent keyEvent = (KeyEvent) msg.obj;
                            setKeyEvent(keyEvent);
                            break;
                        case SET_ENABLE:
                            GVRGazeCursorController.super.setEnable(msg.arg1 == ENABLE);
                            break;
                        case SET_SCENE:
                            GVRGazeCursorController.super.setScene((GVRScene) msg.obj);
                            break;
                        case SEND_INVALIDATE:
                            GVRGazeCursorController.super.invalidate();
                            break;
                    }
                    return false;
                }
            });
            mRunning.countDown();
        }

        private void handleMotionEvent(MotionEvent event)
        {
            float eventX = event.getX();
            float eventY = event.getY();
            float eventZ;
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
                actionDownY = eventY;
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
                eventZ = actionDownZ + deltaX * DEPTH_SENSITIVITY;

                if (eventZ <= getNearDepth())
                {
                    eventZ = getNearDepth();
                }
                if (eventZ >= getFarDepth())
                {
                    eventZ = getFarDepth();
                }
                if (mCursorControl == CursorControl.CURSOR_DEPTH_FROM_CONTROLLER)
                {
                    setCursorDepth(eventZ);
                }
                break;
            }
            GVRGazeCursorController.this.setMotionEvent(event);
            GVRGazeCursorController.super.invalidate();
        }

        public void setPosition(float x, float y, float z) {
            synchronized (position) {
                position.x = x;
                position.y = y;
                position.z = z;
            }
            Message msg = Message.obtain(gazeEventHandler, SET_POSITION, position);
            gazeEventHandler.removeMessages(SET_POSITION);
            msg.sendToTarget();
        }

        public void dispatchKeyEvent(KeyEvent keyEvent) {
            Message msg = Message.obtain(gazeEventHandler, SET_KEY_EVENT, keyEvent);
            msg.sendToTarget();
        }

        public void dispatchMotionEvent(MotionEvent motionEvent) {
            Message msg = Message.obtain(gazeEventHandler, SET_MOTION_EVENT, MotionEvent.obtain(motionEvent));
            msg.sendToTarget();
        }

        public void setEnabled(boolean enable) {
            gazeEventHandler.removeMessages(SET_ENABLE);
            Message msg = Message.obtain(gazeEventHandler, SET_ENABLE, enable ? ENABLE : DISABLE, 0);
            msg.sendToTarget();
        }

        void setScene(GVRScene scene){
            gazeEventHandler.removeMessages(SET_SCENE);
            Message msg = Message.obtain(gazeEventHandler, SET_SCENE, scene);
            msg.sendToTarget();
        }

        void sendInvalidate(){
            gazeEventHandler.removeMessages(SEND_INVALIDATE);
            Message msg = Message.obtain(gazeEventHandler, SEND_INVALIDATE);
            msg.sendToTarget();
        }

        Handler getGazeEventHandler() {
            return gazeEventHandler;
        }

        void await() {
            try {
                mRunning.await();
            } catch (final InterruptedException e) {
                throw new RuntimeException("Cannot be interrupted!");
            } finally {
                mRunning = null;
            }
        }
    }
}
