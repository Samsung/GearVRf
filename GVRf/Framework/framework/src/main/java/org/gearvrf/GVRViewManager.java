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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.GVRScript.SplashMode;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.asynchronous.GVRAsynchronousResourceLoader;
import org.gearvrf.debug.GVRFPSTracer;
import org.gearvrf.debug.GVRMethodCallTracer;
import org.gearvrf.debug.GVRStatsLine;
import org.gearvrf.script.GVRScriptManager;
import org.gearvrf.utility.ImageUtils;
import org.gearvrf.utility.Log;
import org.gearvrf.utility.Threads;
import org.gearvrf.utility.VrAppSettings;
import org.gearvrf.io.GVRInputManager;

import android.app.Activity;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;

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
 * Now it's about the GL thread. It lets the user handle the scene by calling the users GVRScript.onStep().
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
class GVRViewManager extends GVRContext implements RotationSensorListener {

    private static final String TAG = Log.tag(GVRViewManager.class);

    protected final Queue<Runnable> mRunnables = new LinkedBlockingQueue<Runnable>();
    protected final Map<Runnable, Integer> mRunnablesPostRender = new HashMap<Runnable, Integer>();

    protected List<GVRDrawFrameListener> mFrameListeners = new CopyOnWriteArrayList<GVRDrawFrameListener>();

    protected GVRScript mScript;
    protected RotationSensor mRotationSensor;

    protected SplashScreen mSplashScreen;

    protected GVRLensInfo mLensInfo;
    protected GVRRenderBundle mRenderBundle = null;
    protected GVRScene mMainScene = null;
    protected GVRScene mNextMainScene = null;
    protected Runnable mOnSwitchMainScene = null;
    protected GVRScene mSensoredScene = null;

    protected long mPreviousTimeNanos = 0l;
    protected float mFrameTime = 0.0f;
    protected final List<Integer> mDownKeys = new ArrayList<Integer>();

    GVRActivity mActivity;
    protected int mCurrentEye;

    private GVRScreenshotCallback mScreenshotCenterCallback = null;
    private GVRScreenshotCallback mScreenshotLeftCallback = null;
    private GVRScreenshotCallback mScreenshotRightCallback = null;
    private GVRScreenshot3DCallback mScreenshot3DCallback = null;
    ByteBuffer mReadbackBuffer = null;
    int mReadbackBufferWidth = 0, mReadbackBufferHeight = 0;
    private final GVRInputManagerImpl mInputManager;
    private final GVREventManager mEventManager;
    private final GVRScriptManager mScriptManager;

    private native void cull(long scene, long camera, long shader_manager);

    private native void makeShadowMaps(long scene, long shader_manager, int width, int height);

    private native void renderCamera(long appPtr, long scene, long camera, long shaderManager,
            long postEffectShaderManager, long postEffectRenderTextureA, long postEffectRenderTextureB);

    private native void readRenderResultNative(long renderTexture, Object readbackBuffer);

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
     * Constructs GVRViewManager object with GVRScript which controls GL
     * activities
     * 
     * @param gvrActivity
     *            Current activity object
     * @param gvrScript
     *            {@link GVRScript} which describes
     * @param distortionDataFileName
     *            distortion filename under assets folder
     */
    GVRViewManager(GVRActivity gvrActivity, GVRScript gvrScript, GVRXMLParser xmlParser) {
        super(gvrActivity);

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

        // Clear singletons and per-run data structures
        resetOnRestart();

        GVRAsynchronousResourceLoader.setup(this);

        /*
         * Links with the script.
         */
        mScript = gvrScript;
        mActivity = gvrActivity;

        /*
         * Starts listening to the sensor.
         */
        mRotationSensor = new RotationSensor(gvrActivity, this);

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
        mLensInfo = new GVRLensInfo(screenWidthPixels, screenHeightPixels, screenWidthMeters, screenHeightMeters,
                vrAppSettings);

        GVRPerspectiveCamera.setDefaultFovY(vrAppSettings.getEyeBufferParms().getFovY());
        // Different width/height aspect ratio makes the rendered screen warped
        // when the screen rotates
        // GVRPerspectiveCamera.setDefaultAspectRatio(mLensInfo
        // .getRealScreenWidthMeters()
        // / mLensInfo.getRealScreenHeightMeters());
        mInputManager = new GVRInputManagerImpl(this, vrAppSettings.useGazeCursorController());

        mEventManager = new GVREventManager(this);
        mScriptManager = new GVRScriptManager(this);

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
    void onPause() {
        Log.v(TAG, "onPause");
        mRotationSensor.onPause();
    }

    /**
     * Called when the activity will start interacting with the user. At this
     * point your activity is at the top of the activity stack, with user input
     * going to it.
     */
    void onResume() {
        Log.v(TAG, "onResume");
        mRotationSensor.onResume();
    }

    /**
     * The final call you receive before your activity is destroyed.
     */
    void onDestroy() {
        Log.v(TAG, "onDestroy");
        mRotationSensor.onDestroy();
        mInputManager.close();
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return mInputManager.dispatchKeyEvent(event);
    }

    public boolean dispatchMotionEvent(MotionEvent event) {
        return mInputManager.dispatchMotionEvent(event);
    }

    /*
     * GL life cycle
     */

    /**
     * Called when the surface changed size. When
     * setPreserveEGLContextOnPause(true) is called in the surface, this is
     * called only once.
     */
    void onSurfaceCreated() {
        Log.v(TAG, "onSurfaceCreated");

        Thread currentThread = Thread.currentThread();

        // Reduce contention with other Android processes
        currentThread.setPriority(Thread.MAX_PRIORITY);

        // we know that the current thread is a GL one, so we store it to
        // prevent non-GL thread from calling GL functions
        mGLThreadID = currentThread.getId();
        mGlDeleterPtr = NativeGLDelete.ctor();

        // Evaluating anisotropic support on GL Thread
        String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        isAnisotropicSupported = extensions.contains("GL_EXT_texture_filter_anisotropic");

        // Evaluating max anisotropic value if supported
        if (isAnisotropicSupported) {
            maxAnisotropicValue = NativeTextureParameters.getMaxAnisotropicValue();
        }

        mPreviousTimeNanos = GVRTime.getCurrentTime();

        /*
         * GL Initializations.
         */
        mRenderBundle = new GVRRenderBundle(this, mLensInfo);
        setMainScene(new GVRScene(this));
    }

    private void renderCamera(long activity_ptr, GVRScene scene, GVRCamera camera, GVRRenderBundle renderBundle) {
        renderCamera(activity_ptr, scene.getNative(), camera.getNative(),
                renderBundle.getMaterialShaderManager().getNative(),
                renderBundle.getPostEffectShaderManager().getNative(),
                renderBundle.getPostEffectRenderTextureA().getNative(),
                renderBundle.getPostEffectRenderTextureB().getNative());
    }

    /**
     * Called when the surface is created or recreated. Avoided because this can
     * be called twice at the beginning.
     */
    void onSurfaceChanged(int width, int height) {
        Log.v(TAG, "onSurfaceChanged");
    }

    void beforeDrawEyes() {
        if (DEBUG_STATS) {
            mStatsLine.startLine();

            mTracerDrawFrame.enter();
            mTracerDrawFrameGap.leave();

            mTracerBeforeDrawEyes.enter();
        }

        mFrameHandler.beforeDrawEyes();

        if (DEBUG_STATS) {
            mTracerBeforeDrawEyes.leave();
        }
    }

    @Override
    public void captureScreenCenter(GVRScreenshotCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback should not be null.");
        } else {
            mScreenshotCenterCallback = callback;
        }
    }

    @Override
    public void captureScreenLeft(GVRScreenshotCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback should not be null.");
        } else {
            mScreenshotLeftCallback = callback;
        }
    }

    @Override
    public void captureScreenRight(GVRScreenshotCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback should not be null.");
        } else {
            mScreenshotRightCallback = callback;
        }
    }

    @Override
    public void captureScreen3D(GVRScreenshot3DCallback callback) {
        mScreenshot3DCallback = callback;
    }

    private void readRenderResult() {
        if (mReadbackBuffer == null) {

            mReadbackBufferWidth = mLensInfo.getFBOWidth();
            mReadbackBufferHeight = mLensInfo.getFBOHeight();
            mReadbackBuffer = ByteBuffer.allocateDirect(mReadbackBufferWidth * mReadbackBufferHeight * 4);
            mReadbackBuffer.order(ByteOrder.nativeOrder());
        }
        readRenderResultNative(mRenderBundle.getPostEffectRenderTextureA().getNative(), mReadbackBuffer);
    }

    private void returnScreenshotToCaller(final GVRScreenshotCallback callback, final int width, final int height) {
        // run the callback function in a background thread
        final byte[] byteArray = Arrays.copyOf(mReadbackBuffer.array(), mReadbackBuffer.array().length);
        Threads.spawn(new Runnable() {
            public void run() {
                final Bitmap capturedBitmap = ImageUtils.generateBitmapFlipV(byteArray, width, height);
                callback.onScreenCaptured(capturedBitmap);
            }
        });
    }

    private void renderOneCameraAndAddToList(final GVRPerspectiveCamera centerCamera, byte[][] byteArrays, int index) {

        renderCamera(mActivity.getNative(), mMainScene, centerCamera, mRenderBundle);
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

    private void returnScreenshot3DToCaller(final GVRScreenshot3DCallback callback, final byte[][] byteArrays,
            final int width, final int height) {

        if (byteArrays.length != 6) {
            throw new IllegalArgumentException("byteArrays length is not 6.");
        } else {
            // run the callback function in a background thread
            Threads.spawn(new Runnable() {
                public void run() {
                    final Bitmap[] bitmapArray = new Bitmap[6];
                    Runnable[] threads = new Runnable[6];

                    for (int i = 0; i < 6; i++) {
                        final int index = i;
                        threads[i] = new Runnable() {
                            public void run() {
                                byte[] bytearray = byteArrays[index];
                                byteArrays[index] = null;
                                Bitmap bitmap = ImageUtils.generateBitmapFlipV(bytearray, width, height);
                                synchronized (this) {
                                    bitmapArray[index] = bitmap;
                                    notify();
                                }
                            }
                        };
                    }

                    for (Runnable thread : threads) {
                        Threads.spawnLow(thread);
                    }

                    for (int i = 0; i < 6; i++) {
                        synchronized (threads[i]) {
                            if (bitmapArray[i] == null) {
                                try {
                                    threads[i].wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    callback.onScreenCaptured(bitmapArray);
                }
            });
        }
    }

    void onDrawEyeView(int eye) {
        mCurrentEye = eye;
        if (!(mSensoredScene == null || !mMainScene.equals(mSensoredScene))) {
            GVRCameraRig mainCameraRig = mMainScene.getMainCameraRig();

            if (eye == 1) {
                if (DEBUG_STATS) {
                    mTracerDrawEyes1.enter();
                }

                GVRCamera rightCamera = mainCameraRig.getRightCamera();
                renderCamera(mActivity.getNative(), mMainScene, rightCamera, mRenderBundle);

                // if mScreenshotRightCallback is not null, capture right eye
                if (mScreenshotRightCallback != null) {
                    readRenderResult();
                    returnScreenshotToCaller(mScreenshotRightCallback, mReadbackBufferWidth, mReadbackBufferHeight);
                    mScreenshotRightCallback = null;
                }

                mActivity.setCamera(rightCamera);

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

                    renderCamera(mActivity.getNative(), mMainScene, centerCamera, mRenderBundle);

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
                renderCamera(mActivity.getNative(), mMainScene, leftCamera, mRenderBundle);

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

                mActivity.setCamera(leftCamera);

                if (DEBUG_STATS) {
                    mTracerDrawEyes2.leave();
                }
            }
        }
    }

    /** Called once per frame, before {@link #onDrawEyeView(int, float)}. */
    void onDrawFrame() {
        GVRPerspectiveCamera centerCamera = mMainScene.getMainCameraRig().getCenterCamera();
        makeShadowMaps(mMainScene.getNative(), mRenderBundle.getMaterialShaderManager().getNative(),
                mRenderBundle.getPostEffectRenderTextureA().getWidth(),
                mRenderBundle.getPostEffectRenderTextureB().getHeight());
        cull(mMainScene.getNative(), centerCamera.getNative(), mRenderBundle.getMaterialShaderManager().getNative());

        if (mCurrentEye == 1) {
            mActivity.setCamera(mMainScene.getMainCameraRig().getLeftCamera());
        } else {
            mActivity.setCamera(mMainScene.getMainCameraRig().getRightCamera());
        }
    }

    void afterDrawEyes() {
        if (DEBUG_STATS) {
            // Time afterDrawEyes from here
            mTracerAfterDrawEyes.enter();
        }

        // Execute post-rendering tasks (after drawing eyes, but
        // before afterDrawEyes handlers)
        synchronized (mRunnablesPostRender) {
            for (Iterator<Map.Entry<Runnable, Integer>> it = mRunnablesPostRender.entrySet().iterator(); it
                    .hasNext();) {
                Map.Entry<Runnable, Integer> entry = it.next();
                if (entry.getValue() <= 0) {
                    entry.getKey().run();
                    it.remove();
                } else {
                    entry.setValue(entry.getValue() - 1);
                }
            }
        }

        mFrameHandler.afterDrawEyes();

        if (DEBUG_STATS) {
            mTracerAfterDrawEyes.leave();

            mTracerDrawFrame.leave();
            mTracerDrawFrameGap.enter();

            mFPSTracer.tick();
            mStatsLine.printLine(DEBUG_STATS_PERIOD_MS);

            mMainScene.addStatMessage(System.lineSeparator() + mStatsLine.getStats(GVRStatsLine.FORMAT.MULTILINE));
        }
    }

    /*
     * Splash screen life cycle
     */

    /**
     * Efficient handling of the state machine.
     * 
     * We want to be able to show an animated splash screen after
     * {@link GVRScript#onInit(GVRContext) onInit().} That means our frame
     * handler acts differently on the very first frame than it does during
     * splash screen animations, and differently again when we get to normal
     * mode. If we used a state enum and a switch statement, we'd have to keep
     * the two in synch, and we'd be spending render microseconds in a switch
     * statement, vectoring to a call to a handler. Using a interface
     * implementation instead of a state enum, we just call the handler
     * directly.
     */
    protected interface FrameHandler {
        void beforeDrawEyes();

        void afterDrawEyes();
    }

    private FrameHandler firstFrame = new FrameHandler() {

        @Override
        public void beforeDrawEyes() {
            if (mActivity.getAppSettings().showLoadingIcon) {
                mSplashScreen = mScript.createSplashScreen(GVRViewManager.this);
                if (mSplashScreen != null) {
                    getMainScene().addSceneObject(mSplashScreen);
                }
            } else {
                mSplashScreen = null;
            }

            runOnTheFrameworkThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        GVRViewManager.this.getEventManager().sendEvent(
                                mScript, IScriptEvents.class,
                                "onEarlyInit", GVRViewManager.this);

                        GVRViewManager.this.getEventManager().sendEvent(
                                mScript, IScriptEvents.class,
                                "onInit", GVRViewManager.this);

                        if (null != mSplashScreen && SplashMode.AUTOMATIC == mScript.getSplashMode()) {
                            runOnGlThread(new Runnable() {
                                public void run() {
                                    mSplashScreen.closeSplashScreen();
                                }
                            });
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        GVRViewManager.this.runOnGlThread(new Runnable() {
                            public void run() {
                                mActivity.finish();

                                // Just to be safe ...
                                mFrameHandler = splashFrames;
                                firstFrame = null;
                            }
                        });
                    }

                    // Trigger event "onAfterInit" for post-processing of scene
                    // graph after initialization.
                    GVRViewManager.this.getEventManager().sendEvent(
                            mScript, IScriptEvents.class,
                            "onAfterInit");
                }
            });

            if (mSplashScreen == null) {
                // No splash screen, notify main scene now.
                GVRViewManager.this.notifyMainSceneReady();

                mFrameHandler = normalFrames;
                firstFrame = splashFrames = null;
            } else {
                mFrameHandler = splashFrames;
                firstFrame = null;
            }
        }

        @Override
        public void afterDrawEyes() {
        }
    };

    private FrameHandler splashFrames = new FrameHandler() {

        @Override
        public void beforeDrawEyes() {
            // splash screen post-init animations
            long currentTime = doMemoryManagementAndPerFrameCallbacks();

            if (mSplashScreen != null && (currentTime >= mSplashScreen.mTimeout || mSplashScreen.closeRequested())) {
                if (mSplashScreen.closeRequested()
                        || mScript.getSplashMode() == SplashMode.AUTOMATIC) {

                    final SplashScreen splashScreen = mSplashScreen;
                    new GVROpacityAnimation(mSplashScreen, mScript.getSplashFadeTime(), 0) //
                            .setOnFinish(new GVROnFinish() {

                                @Override
                                public void finished(GVRAnimation animation) {
                                    if (mNextMainScene != null) {
                                        setMainScene(mNextMainScene);
                                        // Splash screen finishes. Notify main
                                        // scene it is ready.
                                        GVRViewManager.this.notifyMainSceneReady();
                                    } else {
                                        getMainScene().removeSceneObject(splashScreen);
                                    }

                                    mFrameHandler = normalFrames;
                                    splashFrames = null;
                                }
                            }) //
                            .start(getAnimationEngine());

                    mSplashScreen = null;
                }
            }
        }

        @Override
        public void afterDrawEyes() {
        }
    };

    private final FrameHandler normalFrames = new FrameHandler() {

        public void beforeDrawEyes() {
            mMainScene.resetStats();

            GVRNotifications.notifyBeforeStep();

            doMemoryManagementAndPerFrameCallbacks();

            runOnTheFrameworkThread(new Runnable() {
                public void run() {
                    try {
                        getEventManager().sendEvent(mScript, IScriptEvents.class, "onStep");

                        // Issue "onStep" to the scene
                        GVRViewManager.this.getEventManager().sendEvent(
                            mMainScene, ISceneEvents.class, "onStep");
                    } catch (final Exception exc) {
                        Log.e(TAG, "Exception from onStep: %s", exc.toString());
                        exc.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void afterDrawEyes() {
            GVRNotifications.notifyAfterStep();
            mMainScene.updateStats();
        }
    };

    // Send onInit and onAfterInit events to main scene when it is ready.
    // When there is a splash screen, it is called after the splash screen has
    // completed.
    // If there is no splash screen, it is called after GVRScript.onInit()
    // returns.
    private void notifyMainSceneReady() {
        runOnTheFrameworkThread(new Runnable() {
            @Override
            public void run() {
                // Initialize the main scene
                GVRViewManager.this.getEventManager().sendEvent(
                        mMainScene, ISceneEvents.class,
                        "onInit", GVRViewManager.this, mMainScene);

                // Late-initialize the main scene
                GVRViewManager.this.getEventManager().sendEvent(
                        mMainScene, ISceneEvents.class,
                        "onAfterInit");
            }
        });
    }

    /**
     * This is the code that needs to be executed before either eye is drawn.
     * 
     * @return Current time, from {@link GVRTime#getCurrentTime()}
     */
    private long doMemoryManagementAndPerFrameCallbacks() {
        long currentTime = GVRTime.getCurrentTime();
        mFrameTime = (currentTime - mPreviousTimeNanos) / 1e9f;
        mPreviousTimeNanos = currentTime;

        /*
         * Without the sensor data, can't draw a scene properly.
         */
        if (!(mSensoredScene == null || !mMainScene.equals(mSensoredScene))) {
            Runnable runnable = null;
            while ((runnable = mRunnables.poll()) != null) {
                try {
                    runnable.run();
                } catch (final Exception exc) {
                    Log.e(TAG, "Runnable-on-GL %s threw %s", runnable, exc.toString());
                    exc.printStackTrace();
                }
            }

            final List<GVRDrawFrameListener> frameListeners = mFrameListeners;
            for (GVRDrawFrameListener listener : frameListeners) {
                try {
                    listener.onDrawFrame(mFrameTime);
                } catch (final Exception exc) {
                    Log.e(TAG, "DrawFrameListener %s threw %s", listener, exc.toString());
                    exc.printStackTrace();
                }
            }
        }
        NativeGLDelete.processQueues(mGlDeleterPtr);

        return currentTime;
    }

    protected FrameHandler mFrameHandler = firstFrame;

    void closeSplashScreen() {
        if (mSplashScreen != null) {
            mSplashScreen.closeSplashScreen();
        }
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

    boolean updateSensoredScene() {
        if (mSensoredScene != null && mMainScene.equals(mSensoredScene)) {
            return true;
        }

        if (null != mMainScene) {
            final GVRCameraRig cameraRig = mMainScene.getMainCameraRig();

            if (null != cameraRig && (mSensoredScene == null || !mMainScene.equals(mSensoredScene))) {
                Log.i(TAG, "camera rig yaw reset");
                cameraRig.resetYaw();
                mSensoredScene = mMainScene;
                return true;
            }
        }
        return false;
    }

    /**
     * Called when a key was pressed down and not handled by any of the views
     * inside of the activity.
     * 
     * @param keyCode
     *            The value in {@link event#getKeyCode() event.getKeyCode()}
     * @param event
     *            Description of the key event
     */
    void onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            synchronized (mDownKeys) {
                mDownKeys.add(keyCode);
            }
        }
    }

    @Override
    public GVRScene getMainScene() {
        return mMainScene;
    }

    @Override
    public synchronized void setMainScene(GVRScene scene) {
        mMainScene = scene;
        NativeScene.setMainScene(mMainScene.getNative());

        if (mNextMainScene == scene) {
            mNextMainScene = null;
            if (mOnSwitchMainScene != null) {
                mOnSwitchMainScene.run();
                mOnSwitchMainScene = null;
            }
        }
        if (null != mMainScene) {
            getActivity().setCameraRig(mMainScene.getMainCameraRig());
            mInputManager.setScene(mMainScene);
        }
    }

    @Override
    public synchronized GVRScene getNextMainScene(Runnable onSwitchMainScene) {
        if (mNextMainScene == null) {
            mNextMainScene = new GVRScene(this);
        }
        NativeScene.setMainScene(mNextMainScene.getNative());
        mOnSwitchMainScene = onSwitchMainScene;
        return mNextMainScene;
    }

    @Override
    public boolean isKeyDown(int keyCode) {
        synchronized (mDownKeys) {
            return mDownKeys.contains(keyCode);
        }
    }

    @Override
    public float getFrameTime() {
        return mFrameTime;
    }

    @Override
    public void runOnGlThread(Runnable runnable) {
        if (mGLThreadID == Thread.currentThread().getId()) {
            runnable.run();
        } else {
            mRunnables.add(runnable);
        }
    }

    @Override
    public void runOnGlThreadPostRender(int delayFrames, Runnable runnable) {
        synchronized (mRunnablesPostRender) {
            mRunnablesPostRender.put(runnable, delayFrames);
        }
    }

    @Override
    public void registerDrawFrameListener(GVRDrawFrameListener frameListener) {
        mFrameListeners.add(frameListener);
    }

    @Override
    public void unregisterDrawFrameListener(GVRDrawFrameListener frameListener) {
        mFrameListeners.remove(frameListener);
    }

    @Override
    GVRRenderBundle getRenderBundle() {
        return mRenderBundle;
    }

    @Override
    public GVRInputManager getInputManager() {
        return mInputManager;
    }

    @Override
    public GVREventManager getEventManager() {
        return mEventManager;
    }

    @Override
    public GVRScriptManager getScriptManager() {
        return mScriptManager;
    }

    protected long mGlDeleterPtr;

    @Override
    public void finalize() throws Throwable {
        try {
            if (0 != mGlDeleterPtr) {
                NativeGLDelete.dtor(mGlDeleterPtr);
            }
        } catch (final Exception ignored) {
        } finally {
            super.finalize();
        }
    }

    static {
        // strictly one-time per process op hence the static block
        NativeGLDelete.createTlsKey();
    }

}
