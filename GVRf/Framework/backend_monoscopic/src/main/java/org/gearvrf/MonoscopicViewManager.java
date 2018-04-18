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
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.gearvrf.debug.GVRFPSTracer;
import org.gearvrf.debug.GVRMethodCallTracer;
import org.gearvrf.debug.GVRStatsLine;
import org.gearvrf.io.GVRGearCursorController;
import org.gearvrf.utility.Log;
import org.gearvrf.utility.VrAppSettings;

/*
 * This is the most important part of gvrf.
 * Initialization can be told as 2 parts. A General part and the GL/Vulkan part.
 * The general part needs nothing special but the GL part needs a GL context and Vulkan needs a SurfaceView.
 * GL Part
 *  Since something being done while the GL context creates a surface is time-efficient,
 *  the general initialization is done in the constructor and the GL initialization is
 *  done in onSurfaceCreated().
 * Vulkan Part
 *   Instance of vulkan is created by calling getInstance with surface which is required to create swapchain.
 *   Also a worker thread is created which will do the same work as onDrawFrame of GL.
 * 
 * After the initialization, gvrf works with 2 types of threads.
 * Input threads, and a GL/Vulkan thread.
 * Input threads are about the sensor, joysticks, and keyboards. They send data to gvrf.
 * gvrf handles those data as a message. It saves the data, doesn't do something
 * immediately. That's because gvrf is built to do everything about the scene in the GL thread.
 * There might be some pros by doing some rendering related stuffs outside the GL thread,
 * but since I thought simplicity of the structure results in efficiency, I didn't do that.
 * 
 * Now it's about the GL/Vulkan thread. It lets the user handle the scene by calling the users GVRMain.onStep().
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
class MonoscopicViewManager extends GVRViewManager implements MonoscopicRotationSensorListener {

    static {
        System.loadLibrary("gvrf-monoscopic");
    }

    private static final String TAG = Log.tag(MonoscopicViewManager.class);
    protected MonoscopicRotationSensor mRotationSensor;

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
    private GVRGearCursorController mGearController;


    private MonoscopicSurfaceView mView;
    private int mViewportWidth, mViewportHeight, sampleCount;
    private GVRRenderTarget mRenderTarget[] = new GVRRenderTarget[3];
    private boolean isVulkanInstance = false;
    volatile boolean  activeFlag = true;
    boolean initialized = false;
    private Worker workerObj;
    private Thread vulkanDrawThread;
    private SurfaceView vulkanSurfaceView;

    /**
     * Constructs MonoscopicViewManager object with GVRMain which controls
     * GL activities
     *
     * @param gvrActivity
     *            Current activity object
     * @param gvrMain
     *            {@link GVRMain} which describes
     */
    MonoscopicViewManager(GVRActivity gvrActivity, GVRMain gvrMain,
                             MonoscopicXMLParser xmlParser) {
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
        mRotationSensor = new MonoscopicRotationSensor(gvrActivity, this);


        /*
         * Sets things with the numbers in the xml.
         */

        mRenderTarget[0] = null;

        final VrAppSettings.EyeBufferParams eyeBufferParams = gvrActivity.getAppSettings().getEyeBufferParams();
        GVRPerspectiveCamera.setDefaultFovY(eyeBufferParams.getFovY());

        final DisplayMetrics metrics = new DisplayMetrics();
        gvrActivity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int width = eyeBufferParams.getResolutionWidth();
        if (-1 == width) {
            width = metrics.widthPixels;
        }
        int height = eyeBufferParams.getResolutionHeight();
        if (-1 == height) {
            height = metrics.heightPixels;
        }

        mView = new MonoscopicSurfaceView(gvrActivity, this, width, height);

        float aspect = (float) width / (float) height;
        GVRPerspectiveCamera.setDefaultAspectRatio(aspect);
        mViewportWidth = width;
        mViewportHeight = height;

        sampleCount = eyeBufferParams.getMultiSamples();

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

        if(NativeVulkanCore.useVulkanInstance()){
            isVulkanInstance = true;
            mRenderTarget[0] = null;
            eyeBufferParams.setResolutionWidth(mViewportWidth);
            eyeBufferParams.setResolutionHeight(mViewportHeight);
            eyeBufferParams.setMultiSamples(sampleCount);
            vulkanSurfaceView = new SurfaceView(mActivity);

            vulkanSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    Log.d("Vulkan", "surfaceCreated");
                    workerObj = new Worker();
                    vulkanDrawThread = new Thread(workerObj, "vulkanThread");
                    if(!initialized) {
                        mRenderBundle = makeRenderBundle();
                        final GVRScene scene = null == mMainScene ? new GVRScene(MonoscopicViewManager.this) : mMainScene;
                        mMainScene = scene;
                        NativeScene.setMainScene(scene.getNative());
                        getActivity().setCameraRig(scene.getMainCameraRig());
                        mInputManager.setScene(scene);
                        mRotationSensor.onResume();
                        long vulkanCoreObj;
                        vulkanCoreObj = NativeVulkanCore.getInstance(vulkanSurfaceView.getHolder().getSurface());
                        Thread currentThread = Thread.currentThread();

                        // Reduce contention with other Android processes
                        currentThread.setPriority(Thread.MAX_PRIORITY);

                        if (vulkanCoreObj != 0) {
                            Log.i("Vulkan", "Vulkan Instance On surface created at Vulkan Java Side");
                        } else {
                            Log.i("Vulkan", "Error : On surface  No Instance created at Vulkan Java Side");
                        }

                        initialized = true;
                    }
                    else{
                        NativeVulkanCore.resetTheInstance();
                        NativeVulkanCore.getInstance(vulkanSurfaceView.getHolder().getSurface());
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                    Log.d("Vulkan", "surfaceChanged");
                    if(!vulkanDrawThread.isAlive())
                        vulkanDrawThread.start();
                    activeFlag = true;
                    mRotationSensor.onResume();
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    Log.d("Vulkan", "surfaceDestroyed");
                    activeFlag = false;
                    mRotationSensor.onDestroy();
                }

            });

            gvrActivity.setContentView(vulkanSurfaceView);
        }
        else{
            gvrActivity.setContentView(mView);
        }
    }

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


    GVRRenderTarget getRenderTarget(){
        if(mRenderTarget[0] == null) {
            mRenderTarget[0] = new GVRRenderTarget(new GVRRenderTexture(getActivity().getGVRContext(), mViewportWidth, mViewportHeight, sampleCount, true), getMainScene());
            if(isVulkanInstance) {
                mRenderTarget[1] = new GVRRenderTarget(new GVRRenderTexture(getActivity().getGVRContext(), mViewportWidth, mViewportHeight, sampleCount, true), getMainScene());
                mRenderTarget[2] = new GVRRenderTarget(new GVRRenderTexture(getActivity().getGVRContext(), mViewportWidth, mViewportHeight, sampleCount, true), getMainScene());
            }
        }

        return (isVulkanInstance ? mRenderTarget[NativeVulkanCore.getSwapChainIndexToRender()] : mRenderTarget[0]);
    }

    /*
     * GL life cycle
     */
    protected void onDrawFrame() {
        beforeDrawEyes();
        drawEyes();
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
        mGearController = mInputManager.getGearController();
        if (mGearController != null)
        {
            mGearController.attachReader(new MonoscopicControllerReader());
        }
    }

    public class Worker implements Runnable {
        public void run() {
            while(activeFlag){
                beforeDrawEyes();
                drawEyes();
                afterDrawEyes();
            }
            for(int i = 0; i < 3; i++)
                mRenderTarget[i] = null;
        }
    }

    private void drawEyes() {
        mMainScene.getMainCameraRig().updateRotation();
        GVRRenderTarget renderTarget = getRenderTarget();
        renderTarget.cullFromCamera(mMainScene, mMainScene.getMainCameraRig().getCenterCamera(), mRenderBundle.getShaderManager());
        renderTarget.render(mMainScene, mMainScene
                        .getMainCameraRig().getLeftCamera(), mRenderBundle.getShaderManager(), mRenderBundle.getPostEffectRenderTextureA(),
                mRenderBundle.getPostEffectRenderTextureB());

    }

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


}


class NativeVulkanCore {
    static native long getInstance(Object surface);
    static native int getSwapChainIndexToRender();
    static native void resetTheInstance();
    static native boolean useVulkanInstance();
}
