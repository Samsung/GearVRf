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

package org.gearvrf;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;

import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.debug.GVRFPSTracer;
import org.gearvrf.debug.GVRMethodCallTracer;
import org.gearvrf.debug.GVRStatsLine;
import org.gearvrf.utility.ImageUtils;
import org.gearvrf.utility.Log;
import org.gearvrf.utility.Threads;
import org.gearvrf.utility.VrAppSettings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/*
 * This is the most important part of gvrf.
 * Initialization can be told as 2 parts. A General part and the GL part.
 * The general part needs nothing special but the GL part needs a GL context.
 * Since something being done while the GL context creates a surface is time-efficient,
 * the general initialization is done in the constructor and the GL initialization is
 * done in onSurfaceCreated().
 * 
 * After the initialization, gvrf works with 2 types of threads.
 * Input threads, and a GL thread.
 * Input threads are about the sensor, joysticks, and keyboards. They send data to gvrf.
 * gvrf handles those data as a message. It saves the data, doesn't do something
 * immediately. That's because gvrf is built to do everything about the scene in the GL thread.
 * There might be some pros by doing some rendering related stuffs outside the GL thread,
 * but since I thought simplicity of the structure results in efficiency, I didn't do that.
 * 
 * Now it's about the GL thread. It lets the user handle the scene by calling the users GVRMain.onStep().
 * There are also GVRFrameListeners, GVRAnimationEngine, and Runnables but they aren't that special.
 */

/**
 * This is the core internal class.
 * 
 * It implements {@link GVRContext}. It handles Android application callbacks
 * like cycles such as the standard Android {@link Activity#onResume()},
 * {@link Activity#onPause()}, and {@link Activity#onDestroy()}.
 * 
 * <p>
 * Most importantly, {@link #onDrawFrame()} does the actual rendering, using the
 * current orientation from
 * {@link #onRotationSensor(long, float, float, float, float, float, float, float)
 * onRotationSensor()} to draw the scene graph properly.
 */
class OvrViewManager extends GVRViewManager implements OvrRotationSensorListener {

    private static final String TAG = Log.tag(OvrViewManager.class);

    protected OvrRotationSensor mRotationSensor;
    protected OvrLensInfo mLensInfo;

    protected int mCurrentEye;

    // Statistic debug info
    private GVRStatsLine mStatsLine;
    private GVRFPSTracer mFPSTracer;
    private GVRMethodCallTracer mTracerBeforeDrawEyes;
    private GVRMethodCallTracer mTracerAfterDrawEyes;
    private GVRMethodCallTracer mTracerDrawEyes;
    private GVRMethodCallTracer mTracerDrawEyes1;
    private GVRMethodCallTracer mTracerDrawEyes2;
    private GVRMethodCallTracer mTracerDrawFrame;
    private GVRMethodCallTracer mTracerDrawFrameGap;

    /**
     * Constructs OvrViewManager object with GVRMain which controls GL
     * activities
     * 
     * @param gvrActivity
     *            Current activity object
     * @param gvrMain
     *            {@link GVRMain} which describes
     */
    OvrViewManager(GVRActivity gvrActivity, GVRMain gvrMain, OvrXMLParser xmlParser) {
        super(gvrActivity, gvrMain);

        // Apply view manager preferences
        GVRPreference prefs = GVRPreference.get();
        DEBUG_STATS = prefs.getBooleanProperty(GVRPreference.KEY_DEBUG_STATS, false);
        DEBUG_STATS_PERIOD_MS = prefs.getIntegerProperty(GVRPreference.KEY_DEBUG_STATS_PERIOD_MS, 1000);
        try {
            GVRStatsLine.sFormat = GVRStatsLine.FORMAT
                    .valueOf(prefs.getProperty(GVRPreference.KEY_STATS_FORMAT, GVRStatsLine.FORMAT.DEFAULT.toString()));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        /*
         * Starts listening to the sensor.
         */
        mRotationSensor = new OvrRotationSensor(gvrActivity, this);

        /*
         * Sets things with the numbers in the xml.
         */
        DisplayMetrics metrics = new DisplayMetrics();
        gvrActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final float INCH_TO_METERS = 0.0254f;
        int screenWidthPixels = metrics.widthPixels;
        int screenHeightPixels = metrics.heightPixels;
        float screenWidthMeters = (float) screenWidthPixels / metrics.xdpi * INCH_TO_METERS;
        float screenHeightMeters = (float) screenHeightPixels / metrics.ydpi * INCH_TO_METERS;
        VrAppSettings vrAppSettings = gvrActivity.getAppSettings();
        mLensInfo = new OvrLensInfo(screenWidthPixels, screenHeightPixels, screenWidthMeters, screenHeightMeters,
                vrAppSettings);

        // Debug statistics
        mStatsLine = new GVRStatsLine("gvrf-stats");

        mFPSTracer = new GVRFPSTracer("DrawFPS");
        mTracerDrawFrame = new GVRMethodCallTracer("drawFrame");
        mTracerDrawFrameGap = new GVRMethodCallTracer("drawFrameGap");
        mTracerBeforeDrawEyes = new GVRMethodCallTracer("beforeDrawEyes");
        mTracerDrawEyes = new GVRMethodCallTracer("drawEyes");
        mTracerDrawEyes1 = new GVRMethodCallTracer("drawEyes1");
        mTracerDrawEyes2 = new GVRMethodCallTracer("drawEyes2");
        mTracerAfterDrawEyes = new GVRMethodCallTracer("afterDrawEyes");

        mStatsLine.addColumn(mFPSTracer.getStatColumn());
        mStatsLine.addColumn(mTracerDrawFrame.getStatColumn());
        mStatsLine.addColumn(mTracerDrawFrameGap.getStatColumn());
        mStatsLine.addColumn(mTracerBeforeDrawEyes.getStatColumn());
        mStatsLine.addColumn(mTracerDrawEyes.getStatColumn());
        mStatsLine.addColumn(mTracerDrawEyes1.getStatColumn());
        mStatsLine.addColumn(mTracerDrawEyes2.getStatColumn());
        mStatsLine.addColumn(mTracerAfterDrawEyes.getStatColumn());
    }

    /*
     * Android life cycle
     */

    /**
     * Called when the system is about to start resuming a previous activity.
     * This is typically used to commit unsaved changes to persistent data, stop
     * animations and other things that may be consuming CPU, etc.
     * Implementations of this method must be very quick because the next
     * activity will not be resumed until this method returns.
     */
    @Override
    void onPause() {
        super.onPause();
        Log.v(TAG, "onPause");
        mRotationSensor.onPause();
    }

    /**
     * Called when the activity will start interacting with the user. At this
     * point your activity is at the top of the activity stack, with user input
     * going to it.
     */
    void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
    }

    /**
     * The final call you receive before your activity is destroyed.
     */
    void onDestroy() {
        Log.v(TAG, "onDestroy");
        mRotationSensor.onDestroy();
        super.onDestroy();
    }

    /**
     * Called when the surface is created or recreated. Avoided because this can
     * be called twice at the beginning.
     */
    void onSurfaceChanged(int width, int height) {
        Log.v(TAG, "onSurfaceChanged");
        mRotationSensor.onResume();
    }

    @Override
    protected void beforeDrawEyes() {
        if (DEBUG_STATS) {
            mStatsLine.startLine();

            mTracerDrawFrame.enter();
            mTracerDrawFrameGap.leave();

            mTracerBeforeDrawEyes.enter();
        }

        super.beforeDrawEyes();

        if (DEBUG_STATS) {
            mTracerBeforeDrawEyes.leave();
        }
    }

    private void renderOneCameraAndAddToList(final GVRPerspectiveCamera centerCamera, byte[][] byteArrays, int index) {
        renderCamera(mMainScene, centerCamera, mRenderBundle);
        readRenderResult();
        byteArrays[index] = Arrays.copyOf(mReadbackBuffer.array(), mReadbackBuffer.array().length);
    }

    private void renderSixCamerasAndReadback(final GVRCameraRig mainCameraRig, byte[][] byteArrays) {
        if (byteArrays.length != 6) {
            throw new IllegalArgumentException("byteArrays length is not 6.");
        } else {
            // temporarily create a center camera
            GVRPerspectiveCamera centerCamera = new GVRPerspectiveCamera(this);
            centerCamera.setFovY(90.0f);
            centerCamera.setRenderMask(GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
            GVRSceneObject centerCameraObject = new GVRSceneObject(this);
            centerCameraObject.attachCamera(centerCamera);
            mainCameraRig.getOwnerObject().addChildObject(centerCameraObject);
            GVRTransform centerCameraTransform = centerCameraObject.getTransform();

            int index = 0;
            // render +x face
            centerCameraTransform.rotateByAxis(-90, 0, 1, 0);
            renderOneCameraAndAddToList(centerCamera, byteArrays, index++);

            // render -x face
            centerCameraTransform.rotateByAxis(180, 0, 1, 0);
            renderOneCameraAndAddToList(centerCamera, byteArrays, index++);

            // render +y face
            centerCameraTransform.rotateByAxis(-90, 0, 1, 0);
            centerCameraTransform.rotateByAxis(90, 1, 0, 0);
            renderOneCameraAndAddToList(centerCamera, byteArrays, index++);

            // render -y face
            centerCameraTransform.rotateByAxis(180, 1, 0, 0);
            renderOneCameraAndAddToList(centerCamera, byteArrays, index++);

            // render +z face
            centerCameraTransform.rotateByAxis(90, 1, 0, 0);
            centerCameraTransform.rotateByAxis(180, 0, 1, 0);
            renderOneCameraAndAddToList(centerCamera, byteArrays, index++);

            // render -z face
            centerCameraTransform.rotateByAxis(180, 0, 1, 0);
            renderOneCameraAndAddToList(centerCamera, byteArrays, index++);

            centerCameraObject.detachCamera();
            mainCameraRig.getOwnerObject().removeChildObject(centerCameraObject);
        }
    }

    /**
     * Called from the native side
     * @param eye
     */
    void onDrawEye(int eye) {
        mCurrentEye = eye;
        if (!(mSensoredScene == null || !mMainScene.equals(mSensoredScene))) {
            GVRCameraRig mainCameraRig = mMainScene.getMainCameraRig();

            if (eye == 1) {
                if (DEBUG_STATS) {
                    mTracerDrawEyes1.enter();
                }

                GVRCamera rightCamera = mainCameraRig.getRightCamera();
                renderCamera(mMainScene, rightCamera, mRenderBundle);

                // if mScreenshotRightCallback is not null, capture right eye
                if (mScreenshotRightCallback != null) {
                    readRenderResult();
                    returnScreenshotToCaller(mScreenshotRightCallback, mReadbackBufferWidth, mReadbackBufferHeight);
                    mScreenshotRightCallback = null;
                }

                if (DEBUG_STATS) {
                    mTracerDrawEyes1.leave();
                    mTracerDrawEyes.leave();
                }
            } else {
                if (DEBUG_STATS) {
                    mTracerDrawEyes.enter(); // this eye is drawn first
                    mTracerDrawEyes2.enter();
                }

                // if mScreenshotCenterCallback is not null, capture center eye
                if (mScreenshotCenterCallback != null) {
                    GVRPerspectiveCamera centerCamera = mainCameraRig.getCenterCamera();

                    renderCamera(mMainScene, centerCamera, mRenderBundle);

                    readRenderResult();
                    returnScreenshotToCaller(mScreenshotCenterCallback, mReadbackBufferWidth, mReadbackBufferHeight);

                    mScreenshotCenterCallback = null;
                }

                // if mScreenshot3DCallback is not null, capture 3D screenshot
                if (mScreenshot3DCallback != null) {
                    byte[][] byteArrays = new byte[6][];
                    renderSixCamerasAndReadback(mainCameraRig, byteArrays);
                    returnScreenshot3DToCaller(mScreenshot3DCallback, byteArrays, mReadbackBufferWidth,
                            mReadbackBufferHeight);

                    mScreenshot3DCallback = null;
                }

                GVRCamera leftCamera = mainCameraRig.getLeftCamera();
                renderCamera(mMainScene, leftCamera, mRenderBundle);

                // if mScreenshotLeftCallback is not null, capture left eye
                if (mScreenshotLeftCallback != null) {
                    readRenderResult();
                    returnScreenshotToCaller(mScreenshotLeftCallback, mReadbackBufferWidth, mReadbackBufferHeight);

                    mScreenshotLeftCallback = null;
                }

                if (mScreenshotLeftCallback == null && mScreenshotRightCallback == null
                        && mScreenshotCenterCallback == null && mScreenshot3DCallback == null) {
                    mReadbackBuffer = null;
                }

                if (DEBUG_STATS) {
                    mTracerDrawEyes2.leave();
                }
            }
        }
    }

    /** Called once per frame */
    protected void onDrawFrame() {
        beforeDrawEyes();
        drawEyes(mActivity.getActivityNative().getNative());
        afterDrawEyes();
    }

    @Override
    protected void afterDrawEyes() {
        if (DEBUG_STATS) {
            // Time afterDrawEyes from here
            mTracerAfterDrawEyes.enter();
        }

        super.afterDrawEyes();

        if (DEBUG_STATS) {
            mTracerAfterDrawEyes.leave();

            mTracerDrawFrame.leave();
            mTracerDrawFrameGap.enter();

            mFPSTracer.tick();
            mStatsLine.printLine(DEBUG_STATS_PERIOD_MS);

            mMainScene.addStatMessage(System.lineSeparator() + mStatsLine.getStats(GVRStatsLine.FORMAT.MULTILINE));
        }
    }

    @Override
    void onSurfaceCreated() {
        super.onSurfaceCreated();
        mRotationSensor.onResume();
    }

    /*
     * GVRF APIs
     */

    /**
     * Called to reset current sensor data.
     * 
     * @param timeStamp
     *            current time stamp
     * @param rotationW
     *            Quaternion rotation W
     * @param rotationX
     *            Quaternion rotation X
     * @param rotationY
     *            Quaternion rotation Y
     * @param rotationZ
     *            Quaternion rotation Z
     * @param gyroX
     *            Gyro rotation X
     * @param gyroY
     *            Gyro rotation Y
     * @param gyroZ
     *            Gyro rotation Z
     */
    @Override
    public void onRotationSensor(long timeStamp, float rotationW, float rotationX, float rotationY, float rotationZ,
            float gyroX, float gyroY, float gyroZ) {
        GVRCameraRig cameraRig = null;
        if (mMainScene != null) {
            cameraRig = mMainScene.getMainCameraRig();
        }

        if (cameraRig != null) {
            cameraRig.setRotationSensorData(timeStamp, rotationW, rotationX, rotationY, rotationZ, gyroX, gyroY, gyroZ);
            updateSensoredScene();
        }
    }

    private static native void drawEyes(long ptr);
}
