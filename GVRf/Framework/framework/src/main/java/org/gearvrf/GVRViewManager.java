package org.gearvrf;

import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.io.GVRInputManager;
import org.gearvrf.utility.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

abstract class GVRViewManager extends GVRContext {

    GVRViewManager(GVRActivity activity) {
        super(activity);

        mInputManager = new GVRInputManagerImpl(this, activity.getAppSettings().useGazeCursorController());
        mEventManager = new GVREventManager(this);
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

    protected List<GVRDrawFrameListener> mFrameListeners = new CopyOnWriteArrayList<GVRDrawFrameListener>();

    protected GVRScene mMainScene;
    protected GVRScene mNextMainScene;
    protected Runnable mOnSwitchMainScene;
    protected GVRScene mSensoredScene;

    protected SplashScreen mSplashScreen;

    private final GVREventManager mEventManager;
    private final GVRInputManagerImpl mInputManager;

    private static final String TAG = "GVRViewManager";
}
