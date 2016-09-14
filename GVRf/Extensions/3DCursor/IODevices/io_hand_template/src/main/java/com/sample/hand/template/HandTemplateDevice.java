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
package com.sample.hand.template;

import android.view.KeyEvent;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.io.cursor3d.*;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * This is the base class that handles communication with the input device
 * and instantiates {@link IoDevice} objects for interaction with the application.
 */
public class HandTemplateDevice {
    private static final String TAG = HandTemplateDevice.class.getSimpleName();
    public static final String LEFT_HAND_ID = "left_";
    public static final String RIGHT_HAND_ID = "right_";

    // This is the total count of float values coming in from the native layer,
    // Adjust this value accordingly
    private static int DATA_SIZE = 127;
    private static int BYTE_TO_FLOAT = 4;
    private GVRContext context;
    private Thread thread;
    private ByteBuffer readbackBufferB;
    private FloatBuffer readbackBuffer;
    private IOHand rightHand, leftHand;

    // Create two instances of IoDevice.
    private HandIODevice rightDevice, leftDevice;

    // create Quaternion objects to reduce GC cycles.
    private static final Quaternionf cursorRotation = new Quaternionf();
    private Quaternionf boneRotation = new Quaternionf();

    private Future<GVRTexture> boneTexture;

    /**
     * Construct the Hand device object
     *
     * @param context
     * @param scene
     */
    public HandTemplateDevice(GVRContext context, GVRScene scene) {
        this.context = context;

        // Create a readback buffer and forward it to the native layer
        readbackBufferB = ByteBuffer.allocateDirect(DATA_SIZE * BYTE_TO_FLOAT);
        readbackBufferB.order(ByteOrder.nativeOrder());
        readbackBuffer = readbackBufferB.asFloatBuffer();

        try {
            boneTexture = context.loadFutureTexture(new GVRAndroidResource
                    (context, "cube_texture.png"));
            Future<GVRTexture> jointTexture = context.loadFutureTexture(new GVRAndroidResource
                    (context, "cube_texture.png"));
            rightHand = new IOHand(context);
            leftHand = new IOHand(context);

            // Uncomment the following lines to add scene objects to joints
            //setJointSceneObject(rightHand, jointTexture);
            //setJointSceneObject(leftHand, jointTexture);
            setPalmSceneObject(rightHand, jointTexture);
            setPalmSceneObject(leftHand, jointTexture);
        } catch (IOException e) {
            Log.e(TAG, "IO Exception", e);
        }
        rightDevice = new HandIODevice(RIGHT_HAND_ID + IOFinger.getString(IOFinger.INDEX),
                scene, "Right Hand");
        leftDevice = new HandIODevice(LEFT_HAND_ID + IOFinger.getString(IOFinger.INDEX), scene,
                "Left Hand");

        GVRCameraRig mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.addChildObject(leftHand.getSceneObject());
        mainCameraRig.addChildObject(rightHand.getSceneObject());

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
    }

    void setJointSceneObject(IOHand hand, Future<GVRTexture> jointTexture) {
        for (int i = 0; i < 5; i++) {
            IOFinger finger = hand.getIOFinger(i);
            for (int j = 1; j < 5; j++) {
                // ignore index tip
                if (!(i == IOFinger.INDEX && j == IOJoint.TIP)) {
                    IOJoint joint = finger.getIOJoint(j);
                    joint.setSceneObject(new GVRCubeSceneObject(context, true, jointTexture));
                    // Customize your joint scene object here
                    //joint.setSceneObject();
                }
            }
        }
    }

    void setPalmSceneObject(IOHand hand, Future<GVRTexture> jointTexture) {
        GVRMaterial gvrMaterial = new GVRMaterial(context);
        gvrMaterial.setMainTexture(jointTexture);
        GVRCubeSceneObject palmSceneObject = new GVRCubeSceneObject(context, true, gvrMaterial,
                new Vector3f(5, 3, 1.0f));
        hand.addPalmSceneObject(palmSceneObject);
    }

    private Runnable threadRunnable = new Runnable() {
        @Override
        public void run() {
            // call into native to initialize the thread
            initialize(readbackBufferB);
        }
    };

    /**
     * Perform cleanup using this method.
     */
    public void close() {
        Log.d(TAG, "Interrupting thread");
        // Destroy the native thread.
        destroy();
        thread.interrupt();
    }

    public native void initialize(ByteBuffer readbackBuffer);

    public native void destroy();

    public List<IoDevice> getDeviceList() {
        List<IoDevice> returnList = new ArrayList<IoDevice>();
        returnList.add(leftDevice);
        returnList.add(rightDevice);
        return returnList;
    }

    /**
     * This method is called using JNI
     */
    public void processData() {

        // get number of hands
        float handNum = readbackBuffer.get(0);
        int count = 1;

        boolean rightHandProcessed = false;
        boolean leftHandProcessed = false;

        for (int handCount = 0; handCount < handNum; handCount++) {
            IOHand ioHand;
            HandIODevice device;
            if (handCount == 0) {
                ioHand = rightHand;
                device = rightDevice;
                rightHandProcessed = true;
                GVRSceneObject right = rightHand.getSceneObject();
                if (!right.isEnabled()) {
                    rightHand.getSceneObject().setEnable(true);
                    device.setVisible(true);
                }
            } else {
                ioHand = leftHand;
                device = leftDevice;
                leftHandProcessed = true;
                GVRSceneObject left = leftHand.getSceneObject();
                if (!left.isEnabled()) {
                    leftHand.getSceneObject().setEnable(true);
                    device.setVisible(true);
                }
            }

            //Process Thumbs
            IOFinger ioFinger = ioHand.getIOFinger(IOFinger.THUMB);
            for (int boneNum = 0; boneNum < 4; boneNum++) {
                IOJoint ioJoint = ioFinger.getIOJoint(boneNum + 1);
                if (ioJoint != null) {
                    ioJoint.setPosition(readbackBuffer.get(count), readbackBuffer.get
                            (count + 1), readbackBuffer.get(count + 2));
                }
                count = count + 3;
            }

            setBones(ioFinger);

            if (handCount == 0) {
                // right hand
                for (int fingerNum = 1; fingerNum < 5; fingerNum++) {
                    ioFinger = ioHand.getIOFinger(fingerNum);
                    for (int boneNum = 0; boneNum < 4; boneNum++) {
                        IOJoint ioJoint = ioFinger.getIOJoint(boneNum + 1);
                        if (ioJoint != null) {
                            ioJoint.setPosition(readbackBuffer.get(count), readbackBuffer.get
                                    (count + 1), readbackBuffer.get(count + 2));
                        }
                        count = count + 3;
                    }
                    setBones(ioFinger);
                }
            } else {
                // left hand
                for (int fingerNum = 4; fingerNum > 0; fingerNum--) {
                    ioFinger = ioHand.getIOFinger(fingerNum);
                    for (int boneNum = 0; boneNum < 4; boneNum++) {
                        IOJoint ioJoint = ioFinger.getIOJoint(boneNum + 1);
                        if (ioJoint != null) {
                            ioJoint.setPosition(readbackBuffer.get(count), readbackBuffer.get
                                    (count + 1), readbackBuffer.get(count + 2));
                        }
                        count = count + 3;
                    }
                    setBones(ioFinger);
                }
            }

            //get palm position
            ioHand.getPalmSceneObject().getTransform().setPosition(readbackBuffer.get(count),
                    readbackBuffer.get(count + 1), readbackBuffer.get(count + 2));
            count = count + 3;
            cursorRotation.identity();
            device.process(ioHand.getIOFinger(IOFinger.INDEX).getIOJoint(IOJoint.TIP).getPosition(),
                    cursorRotation);
        }

        if (!rightHandProcessed) {
            GVRSceneObject right = rightHand.getSceneObject();
            if (right.isEnabled()) {
                rightHand.getSceneObject().setEnable(false);
            }
            rightDevice.setVisible(false);
        }
        if (!leftHandProcessed) {
            GVRSceneObject left = leftHand.getSceneObject();
            if (left.isEnabled()) {
                leftHand.getSceneObject().setEnable(false);
            }
            leftDevice.setVisible(false);
        }
    }

    public void setBones(IOFinger ioFinger) {
        //set the positions of the bones
        processBones(ioFinger, IOBone.DISTAL, IOJoint.DIP, IOJoint.TIP);
        processBones(ioFinger, IOBone.INTERMEDIATE, IOJoint.PIP, IOJoint.DIP);
        processBones(ioFinger, IOBone.PROXIMAL, IOJoint.MCP, IOJoint.PIP);
    }

    public void processBones(IOFinger ioFinger, int bone, int prevBone, int nextBone) {
        IOBone ioBone = ioFinger.getIOBone(bone);
        GVRSceneObject boneSceneObject = ioBone.sceneObject;
        Vector3f prev = ioFinger.getIOJoint(prevBone).getPosition();
        Vector3f next = ioFinger.getIOJoint(nextBone).getPosition();
        if (boneSceneObject == null) {
            float len = prev.distance(next);
            if (len == 0.0f) {
                return;
            }

            GVRMaterial material = new GVRMaterial(context);
            material.setMainTexture(boneTexture);
            // Customize your bone scene object here
            // add a cube
            boneSceneObject = new GVRCubeSceneObject(context, true, material, new Vector3f(1.0f,
                    len - 0.5f, 1.0f));
            ioBone.setSceneObject(boneSceneObject);
        }

        Utils.lookAt(prev, next, boneRotation);
        boneRotation.rotate((float) Math.toRadians(90.0f), 0.0f, 0.0f);
        ioBone.setRotation(boneRotation);
        ioBone.setPosition((next.x + prev.x) / 2.0f, (next.y + prev.y) / 2.0f,
                (next.z + prev.z) / 2.0f);
    }

    /**
     * _VENDOR_TODO_ rename the IoDevice here
     * This class is responsible to deliver position and key events to the 3D cursor library.
     * Feel free to re purpose this class to deliver position and key events for a give IODevice.
     *
     * NOTE: In order to prevent reuse of package name. Make sure you change the package name for
     * this class as well.
     */
    private static class HandIODevice extends IoDevice {
        private GVRScene scene;
        private boolean actionDown;

        private final Matrix4f cameraMatrix;
        private static final KeyEvent KEY_DOWN = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent
                .KEYCODE_1);
        private static final KeyEvent KEY_UP = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent
                .KEYCODE_1);

        private static final int VENDOR_ID = 1234;
        private static final int PRODUCT_ID = 5678;
        private static final String VENDOR_NAME = "Template Hand";

        private static final int BUTTON_1 = 0;
        private static final int BUTTON_2 = 1;
        private static final int ACTION_DOWN = 0;
        private static final int ACTION_UP = 1;

        protected HandIODevice(String deviceId, GVRScene scene, String name) {
            /** The last param (boolean) denotes that the device is not ready when this constructor
             * is called. We will use the setConnected call to let the framework know that the
             * device
             * is ready. */
            super(deviceId, VENDOR_ID, PRODUCT_ID, name, VENDOR_NAME, true);
            this.scene = scene;

            // Set the initial position for the cursor here
            setPosition(0.0f, 0.0f, -1.0f);
            cameraMatrix = new Matrix4f();
            setVisible(false);
        }

        @Override
        public void setEnable(boolean enable) {
            // When setPosition to disable (i.e. enable == false) the calls to setPosition and
            // setKeyEvents
            // are ignored by the framework. It is recommended that the events processing be put on
            // hold.
            super.setEnable(enable);
        }

        @Override
        protected void setVisible(boolean value) {
            super.setVisible(value);
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

        public void processKeyEvent(boolean keyEvent) {
            if (keyEvent) {

                if (actionDown == false) {
                    setKeyEvent(KEY_DOWN);
                    actionDown = true;
                }
            } else {
                if (actionDown == true) {
                    setKeyEvent(KEY_UP);
                    actionDown = false;
                }
            }
        }

        public void process(Vector3f position, Quaternionf rotation) {
            cameraMatrix.set(scene.getMainCameraRig().getHeadTransform().getModelMatrix());
            position.mulPoint(cameraMatrix);
            Utils.matrixRotation(cameraMatrix, rotation, rotation);
            super.setPosition(position.x, position.y, position.z);
            super.setRotation(rotation.w, rotation.x, rotation.y, rotation.z);
        }
    }

    static {
        //_VENDOR_TODO_ rename the template_hand_device library here
        System.loadLibrary("template_hand_device");
    }
}


