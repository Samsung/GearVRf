/* Copyright 2016 Samsung Electronics Co., LTD
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

import android.opengl.GLES20;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.asynchronous.GVRAsynchronousResourceLoader;
import org.gearvrf.debug.GVRStatsLine;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.script.GVRScriptManager;
import org.gearvrf.utility.Log;
import org.gearvrf.utility.VrAppSettings;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

abstract class GVRViewManager extends GVRContext {

    GVRViewManager(GVRActivity activity, GVRScript main) {
        super(activity);

        mActivity = activity;
        mMain = main;

        VrAppSettings vrAppSettings = activity.getAppSettings();
        GVRPerspectiveCamera.setDefaultFovY(vrAppSettings.getEyeBufferParams().getFovY());

        // Clear singletons and per-run data structures
        resetOnRestart();

        GVRAsynchronousResourceLoader.setup(this);
        VrAppSettings appSettings = activity.getAppSettings();
        mScriptManager = new GVRScriptManager(this);
        mEventManager = new GVREventManager(this);
        mInputManager = new GVRInputManagerImpl(this, appSettings.useGazeCursorController(),
                appSettings.useAndroidWearTouchpad());
    }

    void onPause() {}

    void onResume() {}

    void onDestroy() {
        mInputManager.close();
    }

    public GVREventManager getEventManager() {
        return mEventManager;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return mInputManager.dispatchKeyEvent(event);
    }

    public boolean dispatchMotionEvent(MotionEvent event) {
        return mInputManager.dispatchMotionEvent(event);
    }

    @Override
    public GVRScene getMainScene() {
        return mMainScene;
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

    void setUseTheFrameworkThread(boolean b) {}

    protected boolean updateSensoredScene() {
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

    public final GVRInputManager getInputManager() {
        return mInputManager;
    }

    final void closeSplashScreen() {
        if (mSplashScreen != null) {
            mSplashScreen.closeSplashScreen();
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
        mRenderBundle = makeRenderBundle();
        setMainScene(new GVRScene(this));
    }

    /**
     * Called on surface creation to create a properly configured render bundle
     * @return
     */
    protected IRenderBundle makeRenderBundle() {
        final VrAppSettings.EyeBufferParams eyeBufferParams = getActivity().getAppSettings().getEyeBufferParams();
        return new GVRRenderBundle(this, eyeBufferParams.getResolutionWidth(), eyeBufferParams.getResolutionHeight());
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

    @Override
    public float getFrameTime() {
        return mFrameTime;
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
    private static class FrameHandler {
        void beforeDrawEyes() {}
        void afterDrawEyes() {}
    }

    private FrameHandler firstFrame = new FrameHandler() {
        @Override
        public void beforeDrawEyes() {
            mMain.setViewManager(GVRViewManager.this);

            if (getActivity().getAppSettings().showLoadingIcon) {
                mSplashScreen = mMain.createSplashScreen();
                if (mSplashScreen != null) {
                    getMainScene().addSceneObject(mSplashScreen);
                }
            } else {
                mSplashScreen = null;
            }

            // execute pending runnables now so any necessary gl calls
            // are done before onInit().  As an example the request to
            // get the GL_MAX_TEXTURE_SIZE needs to be fulfilled.
            synchronized (mRunnables) {
                Runnable runnable = null;
                while ((runnable = mRunnables.poll()) != null) {
                    try {
                        runnable.run();
                    } catch (final Exception exc) {
                        Log.e(TAG, "Runnable-on-GL %s threw %s", runnable, exc.toString());
                        exc.printStackTrace();
                    }
                }
            }

            runOnTheFrameworkThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        getEventManager().sendEvent(mMain, IScriptEvents.class,
                                "onEarlyInit", GVRViewManager.this);

                        getEventManager().sendEvent(mMain, IScriptEvents.class,
                                "onInit", GVRViewManager.this);

                        if (null != mSplashScreen && GVRScript.SplashMode.AUTOMATIC == mMain
                                .getSplashMode() && mMain.getSplashDisplayTime() < 0f) {
                            runOnGlThread(new Runnable() {
                                public void run() {
                                    mSplashScreen.closeSplashScreen();
                                }
                            });
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        runOnGlThread(new Runnable() {
                            public void run() {
                                getActivity().finish();

                                // Just to be safe ...
                                mFrameHandler = splashFrames;
                                firstFrame = null;
                            }
                        });
                    }

                    // Trigger event "onAfterInit" for post-processing of scene
                    // graph after initialization.
                    getEventManager().sendEvent(mMain, IScriptEvents.class,
                            "onAfterInit");
                }
            });

            if (mSplashScreen == null) {
                // No splash screen, notify main scene now.
                notifyMainSceneReady();

                mFrameHandler = normalFrames;
                firstFrame = splashFrames = null;
            } else {
                mFrameHandler = splashFrames;
                firstFrame = null;
            }
        }
    };

    private FrameHandler splashFrames = new FrameHandler() {
        @Override
        public void beforeDrawEyes() {
            // splash screen post-init animations
            long currentTime = doMemoryManagementAndPerFrameCallbacks();

            if (mSplashScreen != null && (currentTime >= mSplashScreen.mTimeout || mSplashScreen.closeRequested())) {
                if (mSplashScreen.closeRequested()
                        || mMain.getSplashMode() == GVRScript.SplashMode.AUTOMATIC) {

                    final SplashScreen splashScreen = mSplashScreen;
                    new GVROpacityAnimation(mSplashScreen, mMain.getSplashFadeTime(), 0) //
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
    };

    private final FrameHandler normalFrames = new FrameHandler() {

        public void beforeDrawEyes() {
            mMainScene.resetStats();

            GVRNotifications.notifyBeforeStep();

            doMemoryManagementAndPerFrameCallbacks();

            runOnTheFrameworkThread(new Runnable() {
                public void run() {
                    try {
                        mMain.onStep();
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
                getEventManager().sendEvent(mMainScene, ISceneEvents.class, "onInit", GVRViewManager.this, mMainScene);

                // Late-initialize the main scene
                getEventManager().sendEvent(mMainScene, ISceneEvents.class, "onAfterInit");
            }
        });
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

    protected void beforeDrawEyes() {
        mFrameHandler.beforeDrawEyes();

        GVRPerspectiveCamera centerCamera = mMainScene.getMainCameraRig().getCenterCamera();
        makeShadowMaps(mMainScene.getNative(), mRenderBundle.getMaterialShaderManager().getNative(),
                mRenderBundle.getPostEffectRenderTextureA().getWidth(),
                mRenderBundle.getPostEffectRenderTextureB().getHeight());
        cull(mMainScene.getNative(), centerCamera.getNative(), mRenderBundle.getMaterialShaderManager().getNative());
    }

    protected void afterDrawEyes() {
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
    }

    protected void renderCamera(GVRScene scene, GVRCamera camera, IRenderBundle
            renderBundle) {
        renderCamera(scene.getNative(), camera.getNative(),
                renderBundle.getMaterialShaderManager().getNative(),
                renderBundle.getPostEffectShaderManager().getNative(),
                renderBundle.getPostEffectRenderTextureA().getNative(),
                renderBundle.getPostEffectRenderTextureB().getNative());
    }

    @Override
    public GVRScriptManager getScriptManager() {
        return mScriptManager;
    }

    @Override
    public GVRMaterialShaderManager getMaterialShaderManager() {
        return mRenderBundle.getMaterialShaderManager();
    }

    @Override
    public GVRPostEffectShaderManager getPostEffectShaderManager() {
        return mRenderBundle.getPostEffectShaderManager();
    }

    protected GVRScreenshotCallback mScreenshotCenterCallback;
    protected GVRScreenshotCallback mScreenshotLeftCallback;
    protected GVRScreenshotCallback mScreenshotRightCallback;
    protected GVRScreenshot3DCallback mScreenshot3DCallback;

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

    private final GVRScriptManager mScriptManager;
    protected final GVRActivity mActivity;
    protected float mFrameTime;
    protected long mPreviousTimeNanos;

    protected FrameHandler mFrameHandler = firstFrame;

    protected List<GVRDrawFrameListener> mFrameListeners = new CopyOnWriteArrayList<GVRDrawFrameListener>();
    protected final Queue<Runnable> mRunnables = new LinkedBlockingQueue<Runnable>();
    protected final Map<Runnable, Integer> mRunnablesPostRender = new HashMap<Runnable, Integer>();

    protected GVRScene mMainScene;
    protected GVRScene mNextMainScene;
    protected Runnable mOnSwitchMainScene;
    protected GVRScene mSensoredScene;

    protected SplashScreen mSplashScreen;

    private final GVREventManager mEventManager;
    private final GVRInputManagerImpl mInputManager;
    protected IRenderBundle mRenderBundle;

    protected GVRScript mMain;

    protected long mGlDeleterPtr;


    protected native void renderCamera(long scene, long camera, long shaderManager,
                                       long postEffectShaderManager, long postEffectRenderTextureA, long postEffectRenderTextureB);
    protected native void cull(long scene, long camera, long shader_manager);
    protected native void makeShadowMaps(long scene, long shader_manager, int width, int height);


    private static final String TAG = "GVRViewManager";
}
