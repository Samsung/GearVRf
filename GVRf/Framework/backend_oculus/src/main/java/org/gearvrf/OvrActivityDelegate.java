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

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.view.KeyEvent;

import org.gearvrf.utility.Log;
import org.gearvrf.utility.VrAppSettings;

/**
 * {@inheritDoc}
 */
final class OvrActivityDelegate implements GVRActivity.GVRActivityDelegate {
    private GVRActivity mActivity;
    private OvrViewManager mActiveViewManager;
    private OvrActivityNative mActivityNative;
    private boolean mUseFallback;

    @Override
    public void onCreate(GVRActivity activity) {
        mActivity = activity;

        mActivityNative = new OvrActivityNative(mActivity, mActivity.getAppSettings(), mRenderingCallbacks);

        try {
            mActivityHandler = new OvrVrapiActivityHandler(activity, mActivityNative, mRenderingCallbacks);
        } catch (final Exception ignored) {
            // GVRf will fallback to GoogleVR in this case.
            mUseFallback = true;
        }
    }

    @Override
    public OvrActivityNative getActivityNative() {
        return mActivityNative;
    }

    @Override
    public GVRViewManager makeViewManager() {
        if (!mUseFallback) {
            return new OvrViewManager(mActivity, mActivity.getScript(), mXmlParser);
        } else {
            return new OvrGoogleVRViewManager(mActivity, mActivity.getScript(), mXmlParser);
        }
    }

    @Override
    public OvrMonoscopicViewManager makeMonoscopicViewManager() {
        return new OvrMonoscopicViewManager(mActivity, mActivity.getScript(), mXmlParser);
    }

    @Override
    public GVRCameraRig makeCameraRig(GVRContext context) {
        return new OvrCameraRig(context);
    }

    @Override
    public GVRConfigurationManager makeConfigurationManager(GVRActivity activity) {
        return new OvrConfigurationManager(activity);
    }

    @Override
    public void parseXmlSettings(AssetManager assetManager, String dataFilename) {
        mXmlParser = new OvrXMLParser(assetManager, dataFilename, mActivity.getAppSettings());
    }

    @Override
    public void onPause() {
        if (null != mActivityHandler) {
            mActivityHandler.onPause();
        }
    }

    @Override
    public void onResume() {
        if (null != mActivityHandler) {
            mActivityHandler.onResume();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    }

    @Override
    public void setScript(GVRScript gvrScript, String dataFileName) {
        if (mUseFallback) {
            mActivityHandler = null;
        } else if (null != mActivityHandler) {
            mActivityHandler.onSetScript();
        }
    }

    @Override
    public void setViewManager(GVRViewManager viewManager) {
        mActiveViewManager = (OvrViewManager)viewManager;
    }

    @Override
    public void onInitAppSettings(VrAppSettings appSettings) {
        if(mUseFallback){
            // This is the only place where the setDockListenerRequired flag can be set before
            // the check in GVRActivity.
            mActivity.getConfigurationManager().setDockListenerRequired(false);
        }
    }

    @Override
    public VrAppSettings makeVrAppSettings() {
        return new OvrVrAppSettings();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

    private long mBackKeyDownTime;
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
            if (KeyEvent.ACTION_DOWN == event.getAction()) {
                boolean result = false;

                if (0 == mBackKeyDownTime) {
                    mBackKeyDownTime = event.getDownTime();
                }

                if (!mActivity.isPaused() && null != mActivityHandler) {
                    if (event.getEventTime() - mBackKeyDownTime >= 750) {
                        result = mActivityHandler.onBackLongPress();
                    }
                }

                return result;
            } else if (KeyEvent.ACTION_UP == event.getAction()) {
                boolean result = false;

                if (!mActivity.isPaused() && null != mActivityHandler) {
                    if (event.getEventTime() - mBackKeyDownTime < 750) {
                        result = mActivityHandler.onBack();
                    }
                }

                mBackKeyDownTime = 0;
                return result;
            }
        }

        return false;
    }

    private final OvrActivityHandlerRenderingCallbacks mRenderingCallbacks = new OvrActivityHandlerRenderingCallbacks() {
        @Override
        public void onSurfaceCreated() {
            mActiveViewManager.onSurfaceCreated();
        }

        @Override
        public void onSurfaceChanged(int width, int height) {
            mActiveViewManager.onSurfaceChanged(width, height);
        }

        @Override
        public void onBeforeDrawEyes() {
            mActiveViewManager.beforeDrawEyes();
            mActiveViewManager.onDrawFrame();
        }

        @Override
        public void onAfterDrawEyes() {
            mActiveViewManager.afterDrawEyes();
        }

        @Override
        public void onDrawEye(int eye) {
            try {
                mActiveViewManager.onDrawEyeView(eye);
            } catch (final Exception e) {
                Log.e(TAG, "error in onDrawEyeView", e);
            }
        }
    };

    private OvrXMLParser mXmlParser;
    private OvrActivityHandler mActivityHandler;
    private final static String TAG = "OvrActivityDelegate";
}
