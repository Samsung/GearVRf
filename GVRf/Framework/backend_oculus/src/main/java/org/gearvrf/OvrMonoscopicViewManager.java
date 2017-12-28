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

import org.gearvrf.utility.Log;
import org.gearvrf.utility.VrAppSettings;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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
class OvrMonoscopicViewManager extends OvrViewManager {

    // private static final String TAG =
    // Log.tag(OvrMonoscopicViewManager.class);

    private OvrSurfaceView mView;
    private int mViewportX, mViewportY, mViewportWidth, mViewportHeight, sampleCount;
    private GVRRenderTarget mRenderTarget[] = new GVRRenderTarget[3];
    private boolean isVulkanInstance = false;
    volatile boolean  activeFlag = true;
    boolean initialized = false;
    private Worker workerObj;
    private Thread vulkanDrawThread;
    private SurfaceView vulkanSurfaceView;

    /**
     * Constructs OvrMonoscopicViewManager object with GVRMain which controls
     * GL activities
     *
     * @param gvrActivity
     *            Current activity object
     * @param gvrMain
     *            {@link GVRMain} which describes
     */
    OvrMonoscopicViewManager(GVRActivity gvrActivity, GVRMain gvrMain,
                             OvrXMLParser xmlParser) {
        super(gvrActivity, gvrMain, xmlParser);

        /*
         * Sets things with the numbers in the xml.
         */

        mRenderTarget[0] = null;
        mView = new OvrSurfaceView(gvrActivity, this, null);

        DisplayMetrics metrics = new DisplayMetrics();
        gvrActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final float INCH_TO_METERS = 0.0254f;
        int screenWidthPixels = metrics.widthPixels;
        int screenHeightPixels = metrics.heightPixels;
        float screenWidthMeters = (float) screenWidthPixels / metrics.xdpi
                * INCH_TO_METERS;
        float screenHeightMeters = (float) screenHeightPixels / metrics.ydpi
                * INCH_TO_METERS;

        mLensInfo = new OvrLensInfo(screenWidthPixels, screenHeightPixels,
                screenWidthMeters, screenHeightMeters,
                gvrActivity.getAppSettings());

        GVRPerspectiveCamera.setDefaultFovY(gvrActivity.getAppSettings()
                .getEyeBufferParams().getFovY());
        int fboWidth = gvrActivity.getAppSettings().getEyeBufferParams()
                .getResolutionWidth();
        int fboHeight = gvrActivity.getAppSettings().getEyeBufferParams()
                .getResolutionHeight();
        if (gvrActivity.getAppSettings().getMonoscopicModeParams()
                .isMonoFullScreenMode()) {
            fboWidth = screenWidthPixels;
            fboHeight = screenHeightPixels;
        } else if (fboWidth <= 0 || fboHeight <= 0) {
            fboWidth = fboHeight = VrAppSettings.DEFAULT_FBO_RESOLUTION;
        }
        float aspect = (float) fboWidth / (float) fboHeight;
        GVRPerspectiveCamera.setDefaultAspectRatio(aspect);
        mViewportX = 0;
        mViewportY = 0;
        mViewportWidth = fboWidth;
        mViewportHeight = fboHeight;

        mLensInfo.setFBOWidth(mViewportWidth);
        mLensInfo.setFBOHeight(mViewportHeight);

        if (fboWidth != screenWidthPixels) {
            mViewportX = (screenWidthPixels / 2) - (fboWidth / 2);
        }
        if (fboHeight != screenHeightPixels) {
            mViewportY = (screenHeightPixels / 2) - (fboHeight / 2);
        }

        sampleCount = gvrActivity.getAppSettings().getEyeBufferParams().getMultiSamples();

        if(NativeVulkanCore.useVulkanInstance()){
            isVulkanInstance = true;
            mRenderTarget[0] = null;
            gvrActivity.getAppSettings().getEyeBufferParams().setResolutionWidth(mViewportWidth);
            gvrActivity.getAppSettings().getEyeBufferParams().setResolutionHeight(mViewportHeight);
            gvrActivity.getAppSettings().getEyeBufferParams().setMultiSamples(sampleCount);
            vulkanSurfaceView = new SurfaceView(mActivity);

            vulkanSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    Log.d("Vulkan", "surfaceCreated");
                    workerObj = new Worker();
                    vulkanDrawThread = new Thread(workerObj, "vulkanThread");
                    if(!initialized) {
                        mRenderBundle = makeRenderBundle();
                        final GVRScene scene = null == mMainScene ? new GVRScene(OvrMonoscopicViewManager.this) : mMainScene;
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
    @Override
    protected void onDrawFrame() {
        beforeDrawEyes();
        drawEyes();
        afterDrawEyes();
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
        renderTarget.cullFromCamera(mMainScene, mMainScene.getMainCameraRig().getCenterCamera(), mRenderBundle.getMaterialShaderManager());
        renderTarget.render(mMainScene, mMainScene
                        .getMainCameraRig().getLeftCamera(), mRenderBundle.getMaterialShaderManager(), mRenderBundle.getPostEffectRenderTextureA(),
                mRenderBundle.getPostEffectRenderTextureB());

    }

}


class NativeVulkanCore {
    static native long getInstance(Object surface);
    static native int getSwapChainIndexToRender();
    static native void resetTheInstance();
    static native boolean useVulkanInstance();
}