/*
 * Copyright (c) 2016. Samsung Electronics Co., LTD
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sample.template;

import android.os.Handler;
import android.view.KeyEvent;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.io.cursor3d.IoDevice;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * _VENDOR_TODO_ rename the IoDevice here
 * This class is responsible to deliver position and key events to the 3D cursor library.
 * Feel free to re purpose this class to deliver position and key events for a give IODevice.
 *
 * NOTE: In order to prevent reuse of package name. Make sure you change the package name for
 * this class as well.
 */
public class TemplateDevice extends IoDevice {
    private static final String TAG = TemplateDevice.class.getSimpleName();
    //Change this value to control the maximum depth for the cursor
    private static final int MAX_DEPTH = 20;
    //_VENDOR_TODO_ enter device details below
    private static final int VENDOR_ID = 1234;
    private static final int PRODUCT_ID = 5678;
    private static final String VENDOR_NAME = "Template Company";

    private static final int BUTTON_1 = 0;
    private static final int BUTTON_2 = 1;
    private static final int ACTION_DOWN = 0;
    private static final int ACTION_UP = 1;

    private static final int SET_CONNECTED_DELAY = 1000;

    private GVRContext context;
    private Thread thread;

    public TemplateDevice(GVRContext context, String deviceId, String name) {
        /** The last param (boolean) denotes that the device is not ready when this constructor
         * is called. We will use the setConnected call to let the framework know that the device
         * is ready. */
        super(deviceId, VENDOR_ID, PRODUCT_ID, name, VENDOR_NAME, false);
        this.context = context;

        // Set the initial position for the cursor here
        setPosition(0.0f, 0.0f, -1.0f);

        /**
         * It is important that we create a new thread so that the GL and the Main Android threads
         * are not impacted by the events generated with by the IO device.
         *
         * We are intentionally creating a thread in Java since it is easier to maintain. Threads
         * created in C++ need to be explicitly registered with the JVM. Here is more information
         * on creating threads in C++ if you wish to do so:
         * http://android.wooyd.org/JNIExample/
         */
        Log.d(TAG, "Creating a new thread");
        thread = new Thread(threadRunnable);
        thread.start();

        /**
         * Simulate startup sequence using a delayed handler, use the {@link #setConnected
         * (boolean)} call to let the framework know that the device is ready.
         */
        Handler handler = new Handler(context.getActivity().getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Send out setConnected");
                TemplateDevice.this.setConnected(true);
            }
        }, SET_CONNECTED_DELAY);
    }

    private Runnable threadRunnable = new Runnable() {
        @Override
        public void run() {
            // call into native to initialize the thread
            initialize();
        }
    };

    /**
     * This is a convenience wrapper around the {@link #setPosition(float, float, float)} call.
     * This method applies the cameras model matrix to the x, y, z to give relative positions
     * with respect to the camera rig.
     *
     * This call is made from the native layer.
     *
     * @param x normalized values for the x axis. This values is adjusted for the frustum in this
     *          method.
     * @param y normalized values for the y axis. This values is adjusted for the frustum in this
     *          method.
     * @param z normalized values for the z axis. This values is multiplied by {@link #MAX_DEPTH}
     *          for the absolute position.
     */
    public void processPosition(float x, float y, float z) {
        GVRScene scene = context.getMainScene();
        if (scene != null) {
            float depth = z * MAX_DEPTH;
            float frustumWidth, frustumHeight;
            // calculate the frustum using the aspect ratio and FOV
            // http://docs.unity3d.com/Manual/FrustumSizeAtDistance.html
            float aspectRatio = scene.getMainCameraRig().getCenterCamera()
                    .getAspectRatio();
            float fovY = scene.getMainCameraRig().getCenterCamera()
                    .getFovY();
            float frustumHeightMultiplier = (float) Math
                    .tan(Math.toRadians(fovY / 2)) * 2.0f;
            frustumHeight = frustumHeightMultiplier * depth;
            frustumWidth = frustumHeight * aspectRatio;

            Matrix4f viewMatrix = scene.getMainCameraRig().getHeadTransform()
                    .getModelMatrix4f();
            Vector3f position = new Vector3f(frustumWidth * -x,
                    frustumHeight * -y, depth);
            position.mulPoint(viewMatrix);
            setPosition(position.x, position.y, position.z);
        }
    }

    /**
     * Synthesize and forward a KeyEvent to the library.
     *
     * This call is made from the native layer.
     *
     * @param code   id of the button
     * @param action integer representing the action taken on the button
     */
    public void dispatchKeyEvent(int code, int action) {
        int keyCode = 0;
        int keyAction = 0;
        if (code == BUTTON_1) {
            keyCode = KeyEvent.KEYCODE_BUTTON_1;
        } else if (code == BUTTON_2) {
            keyCode = KeyEvent.KEYCODE_BUTTON_2;
        }

        if (action == ACTION_DOWN) {
            keyAction = KeyEvent.ACTION_DOWN;
        } else if (action == ACTION_UP) {
            keyAction = KeyEvent.ACTION_UP;
        }

        KeyEvent keyEvent = new KeyEvent(keyAction, keyCode);
        setKeyEvent(keyEvent);
    }

    /**
     * Perform cleanup using this method.
     */
    public void close() {
        Log.d(TAG, "Interrupting thread");
        // Destroy the native thread.
        destroy();
    }

    @Override
    public void setEnable(boolean enable) {
        // When set to disable (i.e. enable == false) the calls to setPosition and setKeyEvents
        // are ignored by the framework. It is recommended that the events processing be put on
        // hold.
        super.setEnable(enable);
    }

    public native void initialize();

    public native void destroy();

    static {
        //_VENDOR_TODO_ rename the template_device library here
        System.loadLibrary("template_device");
    }
}


