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

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.gearvrf.GVRScript.SplashMode;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.asynchronous.GVRAsynchronousResourceLoader;
import org.gearvrf.utility.Log;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;

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
class GVRMonoViewManager extends GVRContext implements RotationSensorListener {

    private static final String TAG = Log.tag(GVRMonoViewManager.class);

    private final Queue<Runnable> mRunnables = new LinkedBlockingQueue<Runnable>();

    private final Object[] mFrameListenersLock = new Object[0];
    private List<GVRDrawFrameListener> mFrameListeners = new ArrayList<GVRDrawFrameListener>();

    private final GVRScript mScript;
    private final RotationSensor mRotationSensor;
    private final GVRSurfaceView mView;

    private SplashScreen mSplashScreen;

    private final GVRLensInfo mLensInfo;
    private GVRRenderBundle mRenderBundle = null;
    private GVRScene mMainScene = null;
    private GVRScene mNextMainScene = null;
    private Runnable mOnSwitchMainScene = null;
    private GVRScene mSensoredScene = null;

    private long mPreviousTimeNanos = 0l;
    private float mFrameTime = 0.0f;
    private final List<Integer> mDownKeys = new ArrayList<Integer>();

    private final GVRReferenceQueue mReferenceQueue = new GVRReferenceQueue();
    private final GVRRecyclableObjectProtector mRecyclableObjectProtector = new GVRRecyclableObjectProtector();
    GVRMonoActivity mActivity;
    private int mCurrentEye;
    private int mViewportX, mViewportY, mViewportWidth, mViewportHeight;


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
    GVRMonoViewManager(GVRMonoActivity gvrActivity, GVRScript gvrScript,
            String distortionDataFileName) {
        super(gvrActivity);

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
        GVRXMLParser xmlParser = new GVRXMLParser(gvrActivity.getAssets(),
                distortionDataFileName);
        
        mView = new GVRSurfaceView(gvrActivity, this, null);
        gvrActivity.setContentView(mView);

        DisplayMetrics metrics = new DisplayMetrics();
        gvrActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final float INCH_TO_METERS = 0.0254f;
        int screenWidthPixels = metrics.widthPixels;
        int screenHeightPixels = metrics.heightPixels;
        float screenWidthMeters = (float) screenWidthPixels / metrics.xdpi
                * INCH_TO_METERS;
        float screenHeightMeters = (float) screenHeightPixels / metrics.ydpi
                * INCH_TO_METERS;

        mLensInfo = new GVRLensInfo(screenWidthPixels, screenHeightPixels,
                screenWidthMeters, screenHeightMeters, xmlParser);

        GVRPerspectiveCamera.setDefaultFovY(xmlParser.getFovY());
        int fboWidth = xmlParser.getFBOWidth();
        int fboHeight = xmlParser.getFBOHeight();
        if(fboWidth <= 0) {
            fboWidth = screenWidthPixels;
        }
        if(fboHeight <=0) {
            fboHeight = screenHeightPixels;
        }
        float aspect = (float) fboWidth / (float) fboHeight;
        GVRPerspectiveCamera.setDefaultAspectRatio(aspect);
        mViewportX = 0;
        mViewportY = 0;
        mViewportWidth = fboWidth;
        mViewportHeight = fboHeight;
        if(fboWidth != screenWidthPixels) {
            mViewportX = (screenWidthPixels / 2) - (fboWidth /2);
        }        
        if(fboHeight != screenHeightPixels) {
            mViewportY = (screenHeightPixels / 2) - (fboHeight / 2);
        }
        
        // Different width/height aspect ratio makes the rendered screen warped
        // when the screen rotates
        // GVRPerspectiveCamera.setDefaultAspectRatio(mLensInfo
        // .getRealScreenWidthMeters()
        // / mLensInfo.getRealScreenHeightMeters());
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
        mView.onPause();
        mRotationSensor.onPause();
    }

    /**
     * Called when the activity will start interacting with the user. At this
     * point your activity is at the top of the activity stack, with user input
     * going to it.
     */
    void onResume() {
        Log.v(TAG, "onResume");
        mView.onResume();
        mRotationSensor.onResume();
    }

    /**
     * The final call you receive before your activity is destroyed.
     */
    void onDestroy() {
        Log.v(TAG, "onDestroy");
        mReferenceQueue.onDestroy();
        mRotationSensor.onDestroy();
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

        mPreviousTimeNanos = GVRTime.getCurrentTime();

        /*
         * GL Initializations.
         */
        mRenderBundle = new GVRRenderBundle(this, mLensInfo);
        mMainScene = new GVRScene(this);
    }

    /**
     * Called when the surface is created or recreated. Avoided because this can
     * be called twice at the beginning.
     */
    void onSurfaceChanged(int width, int height) {
        Log.v(TAG, "onSurfaceChanged");
    }

    void onDrawFrame() {
        //Log.v(TAG, "onDrawFrame");
        mFrameHandler.beforeDrawEyes();
        mFrameHandler.onDrawFrame();
        mFrameHandler.afterDrawEyes();
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
    private interface FrameHandler {
        void beforeDrawEyes();
        void onDrawFrame();
        void afterDrawEyes();
    }

    private FrameHandler firstFrame = new FrameHandler() {

        @Override
        public void beforeDrawEyes() {


            mScript.onInit(GVRMonoViewManager.this);

            if (mSplashScreen == null) {
                mFrameHandler = normalFrames;
                firstFrame = splashFrames = null;
            } else {
                mFrameHandler = splashFrames;
                firstFrame = null;
            }
        }
        
        public void onDrawFrame() {
            //Log.v(TAG, "firstFrame, onDrawFrame()");
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

            if (mSplashScreen != null && currentTime >= mSplashScreen.mTimeout) {
                if (mSplashScreen.closeRequested()
                        || mScript.getSplashMode() == SplashMode.AUTOMATIC) {

                    final SplashScreen splashScreen = mSplashScreen;
                    new GVROpacityAnimation(mSplashScreen,
                            mScript.getSplashFadeTime(), 0) //
                            .setOnFinish(new GVROnFinish() {

                                @Override
                                public void finished(GVRAnimation animation) {
                                    if (mNextMainScene != null) {
                                        setMainScene(mNextMainScene);
                                    } else {
                                        getMainScene().removeSceneObject(
                                                splashScreen);
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
        
        public void onDrawFrame() {
            //Log.v(TAG, "splashFrame, onDrawFrame()");

            drawFrame(false);
        }

        @Override
        public void afterDrawEyes() {
        }
    };

    private final FrameHandler normalFrames = new FrameHandler() {

        public void beforeDrawEyes() {
            GVRNotifications.notifyBeforeStep();

            doMemoryManagementAndPerFrameCallbacks();

            mScript.onStep();
        }
        
        public void onDrawFrame() {
            //Log.v(TAG, "normalFrame, onDrawFrame()");

            drawFrame(true);
        }

        @Override
        public void afterDrawEyes() {
            GVRNotifications.notifyAfterStep();
        }
    };
    
    private long drawFrame(boolean onStep) {
        long currentTime = beforeDrawEyes(onStep);
        drawEyes();
        return currentTime;
    }
    
    /**
     * This is the code that needs to be executed before either eye is drawn.
     * 
     * @param onStep
     *            Should we call {@link GVRScript#onStep()}?
     * @return Current time, from {@link GVRTime#getCurrentTime()}
     */
    private long beforeDrawEyes(boolean onStep) {
        //Log.d(TAG, "beforeDrawEyes(%b)", onStep);
        /*
         * Native heap memory, GPU memory management.
         */
        mReferenceQueue.clean();
        mRecyclableObjectProtector.clean();

        long currentTime = GVRTime.getCurrentTime();
        mFrameTime = (currentTime - mPreviousTimeNanos) / 1e9f;
        mPreviousTimeNanos = currentTime;

        /*
         * Without the sensor data, can't draw a scene properly.
         */
        //Log.d(TAG, "MainScene = %s, mSensoredScene = %s", mMainScene, mSensoredScene);
        if (!(mSensoredScene == null || !mMainScene.equals(mSensoredScene))) {
            Runnable runnable = null;
            while ((runnable = mRunnables.poll()) != null) {
                runnable.run();
            }

            synchronized (mFrameListenersLock) {
                final List<GVRDrawFrameListener> frameListeners = mFrameListeners;
                for (GVRDrawFrameListener listener : frameListeners) {
                    listener.onDrawFrame(mFrameTime);
                }
            }

            // FIXME It would be nice to NOT do this test every frame!
            if (onStep) {                
                mScript.onStep();
                synchronized (mDownKeys) {
                    mDownKeys.clear();
                }
            }
        }

        return currentTime;
    }

    private void drawEyes() {
        //Log.d(TAG, "drawEyes()");
        mMainScene.getMainCameraRig().predict(3.5f / 60.0f);
        GVRMonoRenderer.renderCamera(mMainScene, mMainScene.getMainCameraRig().getLeftCamera(),
                mViewportX, mViewportY, mViewportWidth, mViewportHeight, mRenderBundle);
        
    }

    

    /**
     * This is the code that needs to be executed before either eye is drawn.
     * 
     * @return Current time, from {@link GVRTime#getCurrentTime()}
     */
    private long doMemoryManagementAndPerFrameCallbacks() {
        /*
         * Native heap memory, GPU memory management.
         */
        mReferenceQueue.clean();
        mRecyclableObjectProtector.clean();

        long currentTime = GVRTime.getCurrentTime();
        mFrameTime = (currentTime - mPreviousTimeNanos) / 1e9f;
        mPreviousTimeNanos = currentTime;

        /*
         * Without the sensor data, can't draw a scene properly.
         */
        //Log.d(TAG, "MainScene = %s, mSensoredScene = %s", mMainScene, mSensoredScene);
        if (!(mSensoredScene == null || !mMainScene.equals(mSensoredScene))) {
            Runnable runnable = null;
            while ((runnable = mRunnables.poll()) != null) {
                runnable.run();
            }

            synchronized (mFrameListenersLock) {
                final List<GVRDrawFrameListener> frameListeners = mFrameListeners;
                for (GVRDrawFrameListener listener : frameListeners) {
                    listener.onDrawFrame(mFrameTime);
                }
            }
        }

        return currentTime;
    }

    private FrameHandler mFrameHandler = firstFrame;

    void closeSplashScreen() {
        if (mSplashScreen != null) {
            mSplashScreen.close();
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
    public void onRotationSensor(long timeStamp, float rotationW,
            float rotationX, float rotationY, float rotationZ, float gyroX,
            float gyroY, float gyroZ) {
        GVRCameraRig cameraRig = null;
        if (mMainScene != null) {
            cameraRig = mMainScene.getMainCameraRig();
        }

        if (cameraRig != null) {
            Log.d(TAG, "setting rotation sensor data: %f, %f, %f, %f, %f, %f, %f", rotationW, rotationX, rotationY, rotationZ, gyroX, gyroY, gyroZ);
            cameraRig.setRotationSensorData(timeStamp, rotationW, rotationX,
                    rotationY, rotationZ, gyroX, gyroY, gyroZ);

            if (mSensoredScene == null || !mMainScene.equals(mSensoredScene)) {
                cameraRig.resetYaw();
                mSensoredScene = mMainScene;
            }
        }
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
        if (mNextMainScene == scene) {
            mNextMainScene = null;
            if (mOnSwitchMainScene != null) {
                mOnSwitchMainScene.run();
                mOnSwitchMainScene = null;
            }
        }
    }

    @Override
    public synchronized GVRScene getNextMainScene(Runnable onSwitchMainScene) {
        if (mNextMainScene == null) {
            mNextMainScene = new GVRScene(this);
        }
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
        mRunnables.add(runnable);
    }

    @Override
    public void registerDrawFrameListener(GVRDrawFrameListener frameListener) {
        synchronized (mFrameListenersLock) {
            mFrameListeners = new ArrayList<GVRDrawFrameListener>(
                    mFrameListeners);
            mFrameListeners.add(frameListener);
        }
    }

    @Override
    public void unregisterDrawFrameListener(GVRDrawFrameListener frameListener) {
        synchronized (mFrameListenersLock) {
            mFrameListeners = new ArrayList<GVRDrawFrameListener>(
                    mFrameListeners);
            mFrameListeners.remove(frameListener);
        }
    }

    @Override
    GVRReferenceQueue getReferenceQueue() {
        return mReferenceQueue;
    }

    @Override
    GVRRenderBundle getRenderBundle() {
        return mRenderBundle;
    }

    @Override
    GVRRecyclableObjectProtector getRecyclableObjectProtector() {
        return mRecyclableObjectProtector;
    }
}
