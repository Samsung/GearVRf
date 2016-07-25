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

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import org.gearvrf.utility.Log;
import org.gearvrf.utility.VrAppSettings;

/**
 * {@inheritDoc}
 */
public class GVRActivity extends GVRActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mActivityHandler = new VrapiActivityHandler(this, mRenderingCallbacks);
        } catch (final Exception ignored) {
            // will fall back to mono rendering in that case
            mForceMonoscopic = true;
        }
    }

    @Override
    protected final GVRActivityNative makeActivityNative() {
        return new GVRActivityNative(this, getAppSettings(), mRenderingCallbacks);
    }

    @Override
    protected final GVRViewManager makeViewManager(final GVRXMLParser xmlParser) {
        return new GVRViewManager(this, getScript(), xmlParser);
    }

    @Override
    protected final GVRMonoscopicViewManager makeMonoscopicViewManager(final GVRXMLParser xmlParser) {
        return new GVRMonoscopicViewManager(this, getScript(), xmlParser);
    }

    @Override
    protected void onPause() {
        if (null != mActivityHandler) {
            mActivityHandler.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != mActivityHandler) {
            mActivityHandler.onResume();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setScript(GVRScript gvrScript, String dataFileName) {
        super.setScript(gvrScript, dataFileName);

        if (getAppSettings().getMonoscopicModeParams().isMonoscopicMode()) {
            mActivityHandler = null;
        } else if (null != mActivityHandler) {
            mActivityHandler.onSetScript();
        }
    }

    @Override
    protected void onInitAppSettings(VrAppSettings appSettings) {
        if (mForceMonoscopic) {
            appSettings.getMonoscopicModeParams().setMonoscopicMode(true);
        }
        super.onInitAppSettings(appSettings);
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
        if (!isPaused() && KeyEvent.KEYCODE_BACK == keyCode) {
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

    private final ActivityHandlerRenderingCallbacks mRenderingCallbacks = new ActivityHandlerRenderingCallbacks() {
        @Override
        public void onSurfaceCreated() {
            getViewManager().onSurfaceCreated();
        }

        @Override
        public void onSurfaceChanged(int width, int height) {
            getViewManager().onSurfaceChanged(width, height);
        }

        @Override
        public void onBeforeDrawEyes() {
            final GVRViewManager viewManager = getViewManager();
            viewManager.beforeDrawEyes();
            viewManager.onDrawFrame();
        }

        @Override
        public void onAfterDrawEyes() {
            getViewManager().afterDrawEyes();
        }

        @Override
        public void onDrawEye(int eye) {
            try {
                getViewManager().onDrawEyeView(eye);
            } catch (final Exception e) {
                Log.e(TAG, "error in onDrawEyeView", e);
            }
        }
    };

    private ActivityHandler mActivityHandler;
    private boolean mForceMonoscopic;
}
