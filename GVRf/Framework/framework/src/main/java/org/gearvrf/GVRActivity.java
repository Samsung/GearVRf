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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRView;
import org.gearvrf.script.IScriptable;
import org.gearvrf.utility.DockEventReceiver;
import org.gearvrf.utility.GrowBeforeQueueThreadPoolExecutor;
import org.gearvrf.utility.Log;
import org.gearvrf.utility.Threads;
import org.gearvrf.utility.VrAppSettings;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * The typical GVRF application will have a single Android {@link Activity},
 * which <em>must</em> descend from {@link GVRActivity}, not directly from
 * {@code Activity}.
 * 
 * {@code GVRActivity} creates and manages the internal classes which use sensor
 * data to manage a viewpoint, and thus present an appropriate stereoscopic view
 * of your scene graph. {@code GVRActivity} also gives GVRF a full-screen window
 * in landscape orientation with no title bar.
 */
public class GVRActivity extends Activity implements IEventReceiver, IScriptable {

    static {
        System.loadLibrary("gvrf");
    }
    protected static final String TAG = "GVRActivity";

    private GVRViewManager mViewManager;
    private volatile GVRConfigurationManager mConfigurationManager;
    private GVRMain mGVRMain;
    private VrAppSettings mAppSettings;
    private static View mFullScreenView;

    // Group of views that are going to be drawn
    // by some GVRViewSceneObject to the scene.
    private ViewGroup mRenderableViewGroup;
    private IActivityNative mActivityNative;
    private boolean mPaused = true;

    // Send to listeners and scripts but not this object itself
    private static final int SEND_EVENT_MASK =
            GVREventManager.SEND_MASK_ALL & ~GVREventManager.SEND_MASK_OBJECT;

    private GVREventReceiver mEventReceiver = new GVREventReceiver(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        android.util.Log.i(TAG, "onCreate " + Integer.toHexString(hashCode()));
        super.onCreate(savedInstanceState);

        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            for (int i = 0; i < 10; ++i) {
                try {
                    inputStream = getAssets().open("backend_" + i + ".txt");
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    final String line = reader.readLine();
                    Log.i(TAG, "trying backend " + line);
                    final Class<?> aClass = Class.forName(line);

                    mDelegate = (GVRActivityDelegate) aClass.newInstance();
                    mAppSettings = mDelegate.makeVrAppSettings();
                    mDelegate.onCreate(this);

                    break;
                } catch (final Exception exc) {
                    mDelegate = null;
                }
            }

            if (null == mDelegate) {
                throw new IllegalStateException("Fatal error: no backend available");
            }
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }

        if (null != Threads.getThreadPool()) {
            Threads.getThreadPool().shutdownNow();
        }
        Threads.setThreadPool(new GrowBeforeQueueThreadPoolExecutor("gvrf"));

        /*
         * Removes the title bar and the status bar.
         */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mRenderableViewGroup = (ViewGroup) findViewById(android.R.id.content).getRootView();

        mActivityNative = mDelegate.getActivityNative();
    }

    protected void onInitAppSettings(VrAppSettings appSettings) {
        mDelegate.onInitAppSettings(appSettings);
    }

    private void onConfigure(final String dataFilename) {
        mConfigurationManager = mDelegate.makeConfigurationManager(this);
        mConfigurationManager.addDockListener(this);
        mConfigurationManager.configureForExpectedHeadset();
        mDelegate.parseXmlSettings(getAssets(), dataFilename);

        onInitAppSettings(mAppSettings);
    }

    public final VrAppSettings getAppSettings() {
        return mAppSettings;
    }

    public final GVRViewManager getViewManager() {
        return mViewManager;
    }

    final boolean isPaused() {
        return mPaused;
    }

    @Override
    protected void onPause() {
        android.util.Log.i(TAG, "onPause " + Integer.toHexString(hashCode()));

        mDelegate.onPause();
        mPaused = true;
        if (mViewManager != null) {
            mViewManager.onPause();

            mViewManager.getEventManager().sendEventWithMask(
                    SEND_EVENT_MASK,
                    this,
                    IActivityEvents.class,
                    "onPause");
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        android.util.Log.i(TAG, "onResume " + Integer.toHexString(hashCode()));

        mDelegate.onResume();
        mPaused = false;
        super.onResume();
        if (mViewManager != null) {
            mViewManager.onResume();

            mViewManager.getEventManager().sendEventWithMask(
                    SEND_EVENT_MASK,
                    this,
                    IActivityEvents.class,
                    "onResume");
        }
    }

    @Override
    protected void onDestroy() {
        android.util.Log.i(TAG, "onDestroy " + Integer.toHexString(hashCode()));
        if (mViewManager != null) {
            mViewManager.onDestroy();
            mViewManager.getEventManager().sendEventWithMask(
                    SEND_EVENT_MASK,
                    this,
                    IActivityEvents.class,
                    "onDestroy");
            mViewManager = null;
        }
        if (null != mDockEventReceiver) {
            mDockEventReceiver.stop();
        }

        if (null != mConfigurationManager && !mConfigurationManager.isDockListenerRequired()) {
            handleOnUndock();
        }

        if (null != mActivityNative) {
            mActivityNative.onDestroy();
            mActivityNative = null;
        }

        mDockListeners.clear();
        mGVRMain = null;
        mDelegate = null;
        mAppSettings = null;
        mRenderableViewGroup = null;
        mConfigurationManager = null;

        super.onDestroy();
    }

    /**
     * Links {@linkplain GVRMain a script} to the activity; sets the version;
     * 
     * @param gvrMain
     *            An instance of {@link GVRMain} to handle callbacks on the GL
     *            thread.
     * @param dataFileName
     *            Name of the XML file containing the framebuffer parameters. 
     * 
     *            <p>
     *            The XML filename is relative to the application's
     *            {@code assets} directory, and can specify a file in a
     *            directory under the application's {@code assets} directory.
     */
    public void setMain(GVRMain gvrMain, String dataFileName) {
        this.mGVRMain = gvrMain;
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            onConfigure(dataFileName);
            if (!mDelegate.setMain(gvrMain, dataFileName)) {
                Log.w(TAG, "delegate's setMain failed");
                return;
            }

            boolean isMonoscopicMode = mAppSettings.getMonoscopicModeParams().isMonoscopicMode();
            if (!isMonoscopicMode) {
                mViewManager = mDelegate.makeViewManager(mAppSettings.isMultiviewSet());
            } else {
                mViewManager = mDelegate.makeMonoscopicViewManager();
            }
            mDelegate.setViewManager(mViewManager);

            if (mConfigurationManager.isDockListenerRequired()) {
                startDockEventReceiver();
            } else {
                handleOnDock();
            }

            mViewManager.getEventManager().sendEventWithMask(
                    SEND_EVENT_MASK,
                    this,
                    IActivityEvents.class,
                    "onSetMain", gvrMain);

            final GVRConfigurationManager localConfigurationManager = mConfigurationManager;
            if (!isMonoscopicMode) {
                if (null != mDockEventReceiver && localConfigurationManager.isDockListenerRequired()) {
                    getGVRContext().registerDrawFrameListener(new GVRDrawFrameListener() {
                        @Override
                        public void onDrawFrame(float frameTime) {
                            if (localConfigurationManager.isHmtConnected()) {
                                handleOnDock();
                                getGVRContext().unregisterDrawFrameListener(this);
                            }
                        }
                    });
                }
            }
        } else {
            throw new IllegalArgumentException(
                    "You can not set orientation to portrait for GVRF apps.");
        }
    }

    /**
     * Invalidating just the GVRView associated with the GVRViewSceneObject
     * incorrectly set the clip rectangle to just that view. To fix this,
     * we have to create a full screen android View and invalidate this
     * to restore the clip rectangle.
     * @return full screen View object
     */
    public View getFullScreenView() {
        if (mFullScreenView != null) {
            return mFullScreenView;
        }

        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final int screenWidthPixels = Math.max(metrics.widthPixels, metrics.heightPixels);
        final int screenHeightPixels = Math.min(metrics.widthPixels, metrics.heightPixels);

        final ViewGroup.LayoutParams layout = new ViewGroup.LayoutParams(screenWidthPixels, screenHeightPixels);
        mFullScreenView = new View(this);
        mFullScreenView.setLayoutParams(layout);
        mRenderableViewGroup.addView(mFullScreenView);

        return mFullScreenView;
    }

    /**
     * Gets the {@linkplain GVRMain} linked to the activity.
     * @return the {@link GVRMain}.
     */
    public final GVRMain getMain() {
        return mGVRMain;
    }

    /**
     * Uses the default configuration file that comes with the framework.
     * @see GVRActivity#setMain(GVRMain, String)
     */
    public final void setMain(GVRMain gvrMain) {
        setMain(gvrMain, "_gvr.xml");
    }

    /**
     * Sets whether to force rendering to be single-eye, monoscopic view.
     * 
     * @param force
     *            If true, will create a OvrMonoscopicViewManager when
     *            {@linkplain GVRActivity#setMain(GVRMain, String)} is called. If false, will
     *            proceed to auto-detect whether the device supports VR
     *            rendering and choose the appropriate ViewManager. This call
     *            will only have an effect if it is called before
     *            {@linkplain #setMain(GVRMain, String) setMain()}.
     * @deprecated
     */
    @Deprecated
    public void setForceMonoscopic(boolean force) {
        mAppSettings.getMonoscopicModeParams().setMonoscopicMode(force);
    }

    /**
     * Returns whether a monoscopic view was asked to be forced during
     * {@linkplain #setMain(GVRMain, String) setMain()}.
     * 
     * @see GVRActivity#setForceMonoscopic(boolean)
     * @deprecated
     */
    @Deprecated
    public final boolean getForceMonoscopic() {
        return mAppSettings.getMonoscopicModeParams().isMonoscopicMode();
    }

    final long getNative() {
        return null != mActivityNative ? mActivityNative.getNative() : 0;
    }

    final IActivityNative getActivityNative() {
        return mActivityNative;
    }

    final void setCameraRig(GVRCameraRig cameraRig) {
        if (null != mActivityNative) {
            mActivityNative.setCameraRig(cameraRig);
        }
    }

    private long mBackKeyDownTime;
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int keyAction = event.getAction();
        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
            if (KeyEvent.ACTION_DOWN == keyAction) {
                if (0 == mBackKeyDownTime) {
                    mBackKeyDownTime = event.getDownTime();
                }
            } else if (KeyEvent.ACTION_UP == keyAction) {
                final long duration = event.getEventTime() - mBackKeyDownTime;
                mBackKeyDownTime = 0;
                if (!isPaused()) {
                    if (duration < 250) {
                        if (!mGVRMain.onBackPress()) {
                            if (!mDelegate.onBackPress()) {
                                mViewManager.getActivity().finish();
                            }
                        }
                    }
                }
            }
            return true;
        } else {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (keyAction == KeyEvent.ACTION_DOWN) {
                        final AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                AudioManager.ADJUST_RAISE, 0);
                        return true;
                    }
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (keyAction == KeyEvent.ACTION_DOWN) {
                        final AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                AudioManager.ADJUST_LOWER, 0);
                        return true;
                    }
            }
        }
        if (mViewManager.dispatchKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mDelegate.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (mDelegate.onKeyLongPress(keyCode, event)) {
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mDelegate.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        boolean handled = mViewManager.dispatchMotionEvent(event);
        if (handled == false) {
            handled = super.dispatchGenericMotionEvent(event);
        }
        return handled;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean handled = mViewManager.dispatchMotionEvent(event);
        if (handled == false) {
            handled = super.dispatchTouchEvent(event);// VrActivity's
        }

        mViewManager.getEventManager().sendEventWithMask(
                SEND_EVENT_MASK,
                this,
                IActivityEvents.class,
                "dispatchTouchEvent", event);

        return handled;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mDelegate.onConfigurationChanged(newConfig);

        if (mViewManager != null) {
            mViewManager.getEventManager().sendEventWithMask(
                    SEND_EVENT_MASK,
                    this,
                    IActivityEvents.class,
                    "onConfigurationChanged", newConfig);
        }

        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mViewManager != null) {
            mViewManager.getEventManager().sendEventWithMask(
                    SEND_EVENT_MASK,
                    this,
                    IActivityEvents.class,
                    "onTouchEvent", event);
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (mViewManager != null) {
            mViewManager.getEventManager().sendEventWithMask(
                    SEND_EVENT_MASK,
                    this,
                    IActivityEvents.class,
                    "onWindowFocusChanged", hasFocus);
        }

        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setImmersiveSticky();
        }
    }

    // Set Immersive Sticky as described here:
    // https://developer.android.com/training/system-ui/immersive.html
    private void setImmersiveSticky() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /**
     * Called from C++
     */
    final boolean updateSensoredScene() {
        return mViewManager.updateSensoredScene();
    }

    /**
     * It is a convenient function to add a {@link GVRView} to Android hierarchy
     * view. UI thread will call {@link View#draw(android.graphics.Canvas)}
     * to refresh the view when necessary.
     *
     * @param view Is a {@link GVRView} that draw itself into some
     *            {@link GVRViewSceneObject}.
     */
    public final void registerView(final View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != mRenderableViewGroup) {
                    /* The full screen should be updated otherwise just the children's bounds may be refreshed. */
                    mRenderableViewGroup.setClipChildren(false);
                    mRenderableViewGroup.addView(view);
                }
            }
        });
    }

    /**
     * Remove a child view of Android hierarchy view .
     * 
     * @param view View to be removed.
     */
    public final void unregisterView(final View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != mRenderableViewGroup) {
                    mRenderableViewGroup.removeView(view);
                }
            }
        });
    }

    public final GVRContext getGVRContext() {
        return mViewManager;
    }

    @Override
    public final GVREventReceiver getEventReceiver() {
        return mEventReceiver;
    }

    private boolean mIsDocked = false;

    protected final void handleOnDock() {
        Log.i(TAG, "handleOnDock");
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                if (!mIsDocked) {
                    mIsDocked = true;

                    if (null != mActivityNative) {
                        mActivityNative.onDock();
                    }

                    for (final DockListener dl : mDockListeners) {
                        dl.onDock();
                    }
                }
            }
        };
        runOnUiThread(r);
    }

    protected final void handleOnUndock() {
        Log.i(TAG, "handleOnUndock");
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                if (mIsDocked) {
                    mIsDocked = false;

                    if (null != mActivityNative) {
                        mActivityNative.onUndock();
                    }

                    for (final DockListener dl : mDockListeners) {
                        dl.onUndock();
                    }
                }
            }
        };
        runOnUiThread(r);
    }

    public GVRConfigurationManager getConfigurationManager() {
        return mConfigurationManager;
    }

    interface DockListener {
        void onDock();
        void onUndock();
    }

    private final List<DockListener> mDockListeners = new CopyOnWriteArrayList<DockListener>();

    final void addDockListener(final DockListener dl) {
        mDockListeners.add(dl);
    }

    private DockEventReceiver mDockEventReceiver;

    private void startDockEventReceiver() {
        mDockEventReceiver = mConfigurationManager.makeDockEventReceiver(this,
                new Runnable() {
                    @Override
                    public void run() {
                        handleOnDock();
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        handleOnUndock();
                    }
                });
        if (null != mDockEventReceiver) {
            mDockEventReceiver.start();
        } else {
            Log.w(TAG, "dock listener not started");
        }
    }

    private GVRActivityDelegate mDelegate;

    GVRActivityDelegate getDelegate() {
        return mDelegate;
    }

    interface GVRActivityDelegate {
        void onCreate(GVRActivity activity);
        void onPause();
        void onResume();
        void onConfigurationChanged(final Configuration newConfig);

        boolean onKeyDown(int keyCode, KeyEvent event);
        boolean onKeyUp(int keyCode, KeyEvent event);
        boolean onKeyLongPress(int keyCode, KeyEvent event);

        boolean setMain(GVRMain gvrMain, String dataFileName);
        void setViewManager(GVRViewManager viewManager);
        void onInitAppSettings(VrAppSettings appSettings);

        VrAppSettings makeVrAppSettings();
        IActivityNative getActivityNative();
        GVRViewManager makeViewManager(boolean useMultiview);
        GVRViewManager makeMonoscopicViewManager();
        GVRCameraRig makeCameraRig(GVRContext context);
        GVRConfigurationManager makeConfigurationManager(GVRActivity activity);
        void parseXmlSettings(AssetManager assetManager, String dataFilename);

        boolean onBackPress();
    }
}
