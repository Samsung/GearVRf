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
final class MonoscopicActivityDelegate extends GVRActivity.ActivityDelegateStubs {
    @Override
    public void onCreate(GVRActivity activity) {
        if (null == activity) {
            throw new IllegalArgumentException();
        }

        mActivity = activity;
    }

    @Override
    public GVRViewManager makeViewManager() {
        return new MonoscopicViewManager(mActivity, mActivity.getMain(), mXmlParser);
    }

    @Override
    public GVRCameraRig makeCameraRig(GVRContext context) {
        return new GVRCameraRig(context);
    }

    @Override
    public GVRConfigurationManager makeConfigurationManager(GVRActivity activity) {
        return new MonoscopicConfigurationManager(activity);
    }

    @Override
    public void parseXmlSettings(AssetManager assetManager, String dataFilename) {
        mXmlParser = new MonoscopicXMLParser(assetManager, dataFilename, mActivity.getAppSettings());
    }

    @Override
    public boolean onBackPress() {
        return true;
    }

    @Override
    public boolean setMain(GVRMain gvrMain, String dataFileName) {
        return true;
    }

    @Override
    public VrAppSettings makeVrAppSettings() {
        return new MonoscopicVrAppSettings();
    }

    private GVRActivity mActivity;
    private MonoscopicXMLParser mXmlParser;
}
