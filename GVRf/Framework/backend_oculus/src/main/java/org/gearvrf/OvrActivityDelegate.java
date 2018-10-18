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

import org.gearvrf.utility.VrAppSettings;

/**
 * {@inheritDoc}
 */
final class OvrActivityDelegate extends GVRActivity.ActivityDelegateStubs {
    private GVRActivity mActivity;
    private OvrViewManager mActiveViewManager;
    private OvrActivityNative mActivityNative;

    @Override
    public void onCreate(GVRActivity activity) {
        mActivity = activity;

        mActivityNative = new OvrActivityNative(mActivity, mActivity.getAppSettings());
        mActivityHandler = new OvrVrapiActivityHandler(activity, mActivityNative);
    }

    @Override
    public OvrActivityNative getActivityNative() {
        return mActivityNative;
    }

    @Override
    public GVRViewManager makeViewManager() {
        return new OvrViewManager(mActivity, mActivity.getMain(), mXmlParser);
    }

    @Override
    public GVRCameraRig makeCameraRig(GVRContext context) {
        return new GVRCameraRig(context);
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
    public boolean onBackPress() {
        if (null != mActivityHandler) {
            return mActivityHandler.onBack();
        }
        return false;
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
    public void onDestroy() {
        if (null != mActivityHandler) {
            mActivityHandler.onDestroy();
        }
    }

    @Override
    public boolean setMain(GVRMain gvrMain, String dataFileName) {
        if (null != mActivityHandler) {
            mActivityHandler.onSetScript();
        }
        return true;
    }

    @Override
    public void setViewManager(GVRViewManager viewManager) {
        mActiveViewManager = (OvrViewManager)viewManager;
        mActivityHandler.setViewManager(mActiveViewManager);
    }

    @Override
    public VrAppSettings makeVrAppSettings() {
        final VrAppSettings settings = new OvrVrAppSettings();
        final VrAppSettings.EyeBufferParams params = settings.getEyeBufferParams();
        params.setResolutionHeight(VrAppSettings.DEFAULT_FBO_RESOLUTION);
        params.setResolutionWidth(VrAppSettings.DEFAULT_FBO_RESOLUTION);
        return settings;
    }

    private OvrXMLParser mXmlParser;
    private OvrActivityHandler mActivityHandler;
}
