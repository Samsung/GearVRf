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
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRScene;
import org.joml.Vector3f;

import java.util.concurrent.CountDownLatch;

final class GVRGazeCursorController extends GVRBaseController implements GVRDrawFrameListener {
    private static final int TAP_TIMEOUT = 60;
    private static float TOUCH_SQUARE = 8.0f * 8.0f;
    private static final float DEPTH_SENSITIVITY = 0.1f;
    private final GVRContext context;
    private int referenceCount;
    private boolean buttonDownSent;
    private float actionDownX;
    private float actionDownY;
    private float actionDownZ;
    private boolean isEnabled;

    // Used to calculate the absolute position that the controller reports to
    // the user.
    private final Vector3f gazePosition;
    private final Object lock = new Object();
    
    // Saves the relative position of the cursor with respect to the camera.
    private final Vector3f setPosition;
    private EventHandlerThread thread;

    GVRGazeCursorController(GVRContext context,
                                   GVRControllerType controllerType, String name, int vendorId,
                                   int productId) {
        super(controllerType, name, vendorId, productId);
        this.context = context;
        gazePosition = new Vector3f();
        setPosition = new Vector3f();
        thread = new EventHandlerThread();
        isEnabled = isEnabled();
    }

    /*
     * The increment the reference count to let the cursor controller know how
     * many input devices are using this controller.
     */
    void incrementReferenceCount() {
        referenceCount++;
        if (referenceCount == 1 && isEnabled) {
            start();
        }
    }

    private void start(){
        if (!thread.isAlive()) {
            thread.start();
            thread.await();
        }
        context.registerDrawFrameListener(this);
    }

    private void stop(){
        context.unregisterDrawFrameListener(this);
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
        if (referenceCount == 0 && isEnabled) {
            stop();
            return true;
        }

        return false;
    }

    @Override
    protected synchronized void setScene(GVRScene scene) {
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
    synchronized boolean dispatchKeyEvent(KeyEvent event) {
        if (thread.isAlive()) {
            thread.dispatchKeyEvent(event);
        }
        return true;
    }

    @Override
    synchronized boolean dispatchMotionEvent(MotionEvent event) {
        if(!thread.isAlive()){
            return false;
        }

        MotionEvent clone = MotionEvent.obtain(event);
        float eventX = event.getX();
        float eventY = event.getY();
        int action = clone.getAction();
        Handler handler = thread.getGazeEventHandler();
        if (action == MotionEvent.ACTION_DOWN) {
            actionDownX = eventX;
            actionDownY = eventY;
            actionDownZ = setPosition.z;
            // report ACTION_DOWN as a button
            handler.sendEmptyMessageAtTime(EventHandlerThread.SET_KEY_DOWN, event.getDownTime()
                    + TAP_TIMEOUT);
        } else if (action == MotionEvent.ACTION_UP) {
            // report ACTION_UP as a button
            handler.removeMessages(EventHandlerThread.SET_KEY_DOWN);
            if (buttonDownSent) {
                handler.sendEmptyMessage(EventHandlerThread.SET_KEY_UP);
                buttonDownSent = false;
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            float deltaX = eventX - actionDownX;
            float deltaY = eventY - actionDownY;
            float eventZ = actionDownZ + (deltaX * DEPTH_SENSITIVITY);

            if (eventZ >= getNearDepth()) {
                eventZ = getNearDepth();
            }
            if (eventZ <= getFarDepth()) {
                eventZ = getFarDepth();
            }

            synchronized (lock) {
                setPosition.z = eventZ;
            }
            float distance = (deltaX * deltaX) + (deltaY * deltaY);
            if (distance > TOUCH_SQUARE) {
                handler.removeMessages(EventHandlerThread.SET_KEY_DOWN);
            }
        }
        setMotionEvent(clone);
        return true;
    }

    @Override
    public synchronized void setEnable(boolean enable) {
        if (!isEnabled && enable) {
            isEnabled = true;
            if (referenceCount > 0) {
                start();
                thread.setEnabled(true);
            }

        } else if (isEnabled && !enable) {
            isEnabled = false;
            if (referenceCount > 0) {
                thread.setEnabled(false);
                stop();
            }
        }
    }

    @Override
    public void setPosition(float x, float y, float z) {
        setPosition.set(x, y, z);
        thread.setPosition(x, y, z);
    }

    @Override
    public void onDrawFrame(float frameTime) {
        synchronized (lock) {
            setPosition.mulPosition(context.getMainScene().getMainCameraRig()
                    .getHeadTransform().getModelMatrix4f(), gazePosition);
        }
        thread.setPosition(gazePosition.x, gazePosition.y, gazePosition.z);
    }

    void close() {
        if (referenceCount > 0) {
            context.unregisterDrawFrameListener(this);
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
        public static final int SET_KEY_DOWN = 2;
        public static final int SET_KEY_UP = 3;
        public static final int SET_ENABLE = 4;
        public static final int SET_SCENE = 5;
        public static final int SEND_INVALIDATE = 6;

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
                        case SET_KEY_DOWN:
                            buttonDownSent = true;
                            setKeyEvent(BUTTON_GAZE_DOWN);
                            break;
                        case SET_KEY_UP:
                            setKeyEvent(BUTTON_GAZE_UP);
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

        public void setEnabled(boolean enable) {
            gazeEventHandler.removeMessages(SET_ENABLE);
            Message msg = Message.obtain(gazeEventHandler, SET_ENABLE, enable ? ENABLE : DISABLE);
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
