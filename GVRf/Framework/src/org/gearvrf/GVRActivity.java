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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRView;
import org.gearvrf.script.IScriptable;
import org.gearvrf.utility.DockEventReceiver;
import org.gearvrf.utility.Log;
import org.gearvrf.utility.VrAppSettings;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
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

    private static final String TAG = Log.tag(GVRActivity.class);

    // these values are copy of enum KeyEventType in VrAppFramework/Native_Source/Input.h
    public static final int KEY_EVENT_NONE = 0;
    public static final int KEY_EVENT_SHORT_PRESS = 1;
    public static final int KEY_EVENT_DOUBLE_TAP = 2;
    public static final int KEY_EVENT_LONG_PRESS = 3;
    public static final int KEY_EVENT_DOWN = 4;
    public static final int KEY_EVENT_UP = 5;
    public static final int KEY_EVENT_MAX = 6;

    private GVRViewManager mViewManager;
    private GVRScript mGVRScript;
    private VrAppSettings mAppSettings;

    // Group of views that are going to be drawn
    // by some GVRViewSceneObject to the scene.
    private ViewGroup mRenderableViewGroup = null;
    private GVRActivityNative mActivityNative;
    private boolean mPaused = true;

    // Send to listeners and scripts but not this object itself
    private static final int SEND_EVENT_MASK =
            GVREventManager.SEND_MASK_ALL & ~GVREventManager.SEND_MASK_OBJECT;

    private GVREventReceiver mEventReceiver = new GVREventReceiver(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        android.util.Log.i(TAG, "onCreate " + Integer.toHexString(hashCode()));
        /*
         * Removes the title bar and the status bar.
         */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mAppSettings = new VrAppSettings();
        super.onCreate(savedInstanceState);

        mRenderableViewGroup = (ViewGroup) findViewById(android.R.id.content).getRootView();
        mDockEventReceiver = new DockEventReceiver(this,
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
        mDockEventReceiver.start();

        mActivityNative = GVRActivityNative.createObject(this, mAppSettings, mRenderingCallbacks);

        try {
            mActivityHandler = new VrapiActivityHandler(this, mRenderingCallbacks);
        } catch (final Exception ignored) {
            // will fall back to mono rendering in that case
        }
    }

    /**
     * <em>Derived classes must call through to the super class's implementation of this method.</em>
     * </p>
     */
    protected void onInitAppSettings(VrAppSettings appSettings) {
        GVRConfigurationManager.onInitialize(this);
    }

    public VrAppSettings getAppSettings() {
        return mAppSettings;
    }

    @Override
    protected void onPause() {
        android.util.Log.i(TAG, "onPause " + Integer.toHexString(hashCode()));

        mPaused = true;
        if (mViewManager != null) {
            mViewManager.onPause();

            mViewManager.getEventManager().sendEventWithMask(
                    SEND_EVENT_MASK,
                    this,
                    IActivityEvents.class,
                    "onPause");
        }
        if (null != mDockEventReceiver) {
            mDockEventReceiver.stop();
        }
        if (null != mActivityHandler) {
            mActivityHandler.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        android.util.Log.i(TAG, "onResume " + Integer.toHexString(hashCode()));

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
        if (null != mDockEventReceiver) {
            mDockEventReceiver.start();
        }
        if (null != mActivityHandler) {
            mActivityHandler.onResume();
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
        }
        mActivityNative.onDestroy();
        super.onDestroy();
    }

    /**
     * Links {@linkplain GVRScript a script} to the activity; sets the version;
     * sets the lens distortion compensation parameters.
     * 
     * @param gvrScript
     *            An instance of {@link GVRScript} to handle callbacks on the GL
     *            thread.
     * @param distortionDataFileName
     *            Name of the XML file containing the device parameters. We
     *            currently only support the Galaxy Note 4 because that is the
     *            only shipping device with the proper GL extensions. When more
     *            devices support GVRF, we will publish new device files, along
     *            with app-level auto-detect guidelines. This approach will let
     *            you support new devices, using the same version of GVRF that
     *            you have already tested and approved.
     * 
     *            <p>
     *            The XML filename is relative to the application's
     *            {@code assets} directory, and can specify a file in a
     *            directory under the application's {@code assets} directory.
     */
    public void setScript(GVRScript gvrScript, String distortionDataFileName) {
        this.mGVRScript = gvrScript;
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {

            GVRXMLParser xmlParser = new GVRXMLParser(getAssets(),
                    distortionDataFileName, mAppSettings);
            onInitAppSettings(mAppSettings);
            if (null != mActivityHandler && !mAppSettings.getMonoScopicModeParms().isMonoScopicMode()) {
                mViewManager = new GVRViewManager(this, gvrScript, xmlParser);
            } else {
                mActivityHandler = null;    //oculus support not requested
                mViewManager = new GVRMonoscopicViewManager(this, gvrScript,
                        xmlParser);
            }

            mViewManager.getEventManager().sendEventWithMask(
                    SEND_EVENT_MASK,
                    this,
                    IActivityEvents.class,
                    "onSetScript", gvrScript);

            if (null != mActivityHandler) {
                mViewManager.registerDrawFrameListener(new GVRDrawFrameListener() {
                    @Override
                    public void onDrawFrame(float frameTime) {
                        if (GVRConfigurationManager.getInstance().isHmtConnected()) {
                            handleOnDock();
                            mViewManager.unregisterDrawFrameListener(this);
                        }
                    }
                });

                mActivityHandler.onSetScript();
            }
        } else {
            throw new IllegalArgumentException(
                    "You can not set orientation to portrait for GVRF apps.");
        }
    }

    /**
     * Gets the {@linkplain GVRScript a script} linked to the activity.
     * @return the {@link GVRScript}.
     */
    public GVRScript getScript() {
        return mGVRScript;
    }

    /**
     * Sets whether to force rendering to be single-eye, monoscopic view.
     * 
     * @param force
     *            If true, will create a GVRMonoscopicViewManager when
     *            {@linkplain setScript setScript()} is called. If false, will
     *            proceed to auto-detect whether the device supports VR
     *            rendering and choose the appropriate ViewManager. This call
     *            will only have an effect if it is called before
     *            {@linkplain #setScript(GVRScript, String) setScript()}.
     * @deprecated
     */
    @Deprecated
    public void setForceMonoscopic(boolean force) {
        mAppSettings.monoScopicModeParms.setMonoScopicMode(force);
    }

    /**
     * Returns whether a monoscopic view was asked to be forced during
     * {@linkplain #setScript(GVRScript, String) setScript()}.
     * 
     * @see setForceMonoscopic
     * @deprecated
     */
    @Deprecated
    public boolean getForceMonoscopic() {
        return mAppSettings.monoScopicModeParms.isMonoScopicMode();
    }

    public long getNative() {
        return mActivityNative.getNative();
    }

    void oneTimeShutDown() {
        Log.e(TAG, " oneTimeShutDown from native layer");
    }

    void setCamera(GVRCamera camera) {
        mActivityNative.setCamera(camera);
    }

    void setCameraRig(GVRCameraRig cameraRig) {
        mActivityNative.setCameraRig(cameraRig);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled = mViewManager.dispatchKeyEvent(event);
        if (handled == false) {
            handled = super.dispatchKeyEvent(event);
        }
        return handled;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!mPaused && KeyEvent.KEYCODE_BACK == keyCode) {
            if (null != mActivityHandler) {
                return mActivityHandler.onBack();
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            if (null != mActivityHandler) {
                return mActivityHandler.onBackLongPress();
            }
        }
        return super.onKeyLongPress(keyCode, event);
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
    }

    boolean updateSensoredScene() {
        return mViewManager.updateSensoredScene();
    }

    /**
     * It is a convenient function to add a {@link GVRView} to Android hierarchy
     * view. UI thread will call {@link GVRView#draw(android.graphics.Canvas)}
     * to refresh the view when necessary.
     *
     * @param view Is a {@link GVRView} that draw itself into some
     *            {@link GVRViewSceneObject}.
     */
    public void registerView(final View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /* The full screen should be updated
                otherwise just the children's bounds may be refreshed. */
                mRenderableViewGroup.setClipChildren(false);

                mRenderableViewGroup.addView(view);;
            }
        });
    }

    /**
     * Remove a child view of Android hierarchy view .
     * 
     * @param view View to be removed.
     */
    public void unregisterView(final View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRenderableViewGroup.removeView(view);
            }
        });
    }

    public GVRContext getGVRContext() {
        return mViewManager;
    }

    @Override
    public GVREventReceiver getEventReceiver() {
        return mEventReceiver;
    }

    private boolean mIsDocked = false;

    private void handleOnDock() {
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

    private void handleOnUndock() {
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

    interface DockListener {
        void onDock();
        void onUndock();
    }

    private final List<DockListener> mDockListeners = new CopyOnWriteArrayList<DockListener>();

    void addDockListener(final DockListener dl) {
        mDockListeners.add(dl);
    }

    private DockEventReceiver mDockEventReceiver;

    private final ActivityHandlerRenderingCallbacks mRenderingCallbacks = new ActivityHandlerRenderingCallbacks() {
        @Override
        public void onSurfaceCreated() {
            mViewManager.onSurfaceCreated();
        }

        @Override
        public void onSurfaceChanged(int width, int height) {
            mViewManager.onSurfaceChanged(width, height);
        }

        @Override
        public void onBeforeDrawEyes() {
            mViewManager.beforeDrawEyes();
            mViewManager.onDrawFrame();
        }

        @Override
        public void onAfterDrawEyes() {
            mViewManager.afterDrawEyes();
        }

        @Override
        public void onDrawEye(int eye) {
            try {
                mViewManager.onDrawEyeView(eye);
            } catch (final Exception e) {
                Log.e(TAG, "error in onDrawEyeView", e);
            }
        }
    };
    private ActivityHandler mActivityHandler;

}
