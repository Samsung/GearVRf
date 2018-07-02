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
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import org.gearvrf.io.GVRTouchPadGestureListener;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRView;
import org.gearvrf.script.IScriptable;
import org.gearvrf.utility.VrAppSettings;

/**
 * The typical GVRF application will have a single Android {@link Activity},
 * which <em>must</em> descend from {@link GVRActivity}, not directly from
 * {@code Activity}.
 *
 * {@code GVRActivity} creates and manages the internal classes which use sensor
 * data to manage a viewpoint, and thus present an appropriate stereoscopic view
 * of your scene graph. {@code GVRActivity} also gives GVRF a full-screen window
 * in landscape orientation with no title bar.
 *
 * Please consider applying the GVRfAppTheme theme to your activity in the manifest.
 * It takes care of things like disabling the preview (which if enabled may lead to
 * white screen flashing launch time), choosing appropriate background color and
 * making sure the activity is using the full screen.
 */
public class GVRActivity extends Activity implements IEventReceiver, IScriptable {
    private GVRApplication mApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mApplication = new GVRApplication(this);
        super.onCreate(savedInstanceState);
    }

    public final VrAppSettings getAppSettings() {
        return mApplication.getAppSettings();
    }

    public final GVRViewManager getViewManager() {
        return mApplication.getViewManager();
    }

    @Override
    protected void onPause() {
        mApplication.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mApplication.resume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mApplication.destroy();
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
        mApplication.setMain(gvrMain, dataFileName);
    }

    /**
     * Invalidating just the GVRView associated with the GVRViewSceneObject
     * incorrectly set the clip rectangle to just that view. To fix this,
     * we have to create a full screen android View and invalidate this
     * to restore the clip rectangle.
     * @return full screen View object
     */
    public View getFullScreenView() {
        return mApplication.getFullScreenView();
    }

    /**
     * Gets the {@linkplain GVRMain} linked to the activity.
     * @return the {@link GVRMain}.
     */
    public final GVRMain getMain() {
        return mApplication.getMain();
    }

    /**
     * Uses the default configuration file that comes with the framework.
     * @see GVRActivity#setMain(GVRMain, String)
     */
    public final void setMain(GVRMain gvrMain) {
        setMain(gvrMain, "_gvr.xml");
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mApplication.dispatchKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mApplication.keyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (mApplication.keyLongPress(keyCode, event)) {
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mApplication.keyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if(mApplication.dispatchGenericMotionEvent(event)) {
            return true;
        }
        return super.dispatchGenericMotionEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(mApplication.dispatchTouchEvent(event)) {
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mApplication.configurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mApplication.touchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        mApplication.windowFocusChanged(hasFocus);
        super.onWindowFocusChanged(hasFocus);
    }

    /**
     * It is a convenient function to add a {@link GVRView} to Android hierarchy
     * view. UI thread will refresh the view when necessary.
     *
     * @param view Is a {@link GVRView} that draw itself into some
     *            {@link GVRViewSceneObject}.
     */
    public final void registerView(final View view) {
        mApplication.registerView(view);
    }

    /**
     * Remove a child view of Android hierarchy view .
     *
     * @param view View to be removed.
     */
    public final void unregisterView(final View view) {
        mApplication.unregisterView(view);
    }

    public final GVRContext getGVRContext() {
        return mApplication.getGVRContext();
    }

    @Override
    public final GVREventReceiver getEventReceiver() {
        return mApplication.getEventReceiver();
    }

    public GVRConfigurationManager getConfigurationManager() {
        return mApplication.getConfigurationManager();
    }

    /**
     * Enables the Android GestureDetector which in turn fires the appropriate {@link GVRMain} callbacks.
     * By default it is not.
     * @see GVRMain#onSwipe(GVRTouchPadGestureListener.Action, float)
     * @see GVRMain#onSingleTapUp(MotionEvent)
     * @see GVRTouchPadGestureListener
     */
    public synchronized void enableGestureDetector() {
        mApplication.enableGestureDetector();
    }

    public GVRApplication getGVRApplication() {
        return mApplication;
    }
}
