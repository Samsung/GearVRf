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
final class DaydreamActivityDelegate extends GVRApplication.ActivityDelegateStubs implements IActivityNative {
    private GVRApplication mApplication;
    private DaydreamViewManager daydreamViewManager;

    @Override
    public void onCreate(GVRApplication application) {
        mApplication = application;
    }

    @Override
    public IActivityNative getActivityNative() {
        return this;
    }

    @Override
    public GVRViewManager makeViewManager() {
        return new DaydreamViewManager(mApplication, mApplication.getMain());
    }

    @Override
    public GVRCameraRig makeCameraRig(GVRContext context) {
        return new DaydreamCameraRig(context);
    }

    @Override
    public GVRConfigurationManager makeConfigurationManager() {
        return new GVRConfigurationManager(mApplication) {
            @Override
            public boolean isHmtConnected() {
                return false;
            }

            public boolean usingMultiview() {
                return false;
            }
        };
    }

    @Override
    public void onInitAppSettings(VrAppSettings appSettings) {
        // This is the only place where the setDockListenerRequired flag can be set before
        // the check in GVRActivity.
        mApplication.getConfigurationManager().setDockListenerRequired(false);
    }

    @Override
    public void parseXmlSettings(AssetManager assetManager, String dataFilename) {
        new DaydreamXMLParser(assetManager, dataFilename, mApplication.getAppSettings());
    }

    @Override
    public boolean setMain(GVRMain gvrMain, String dataFileName) {
        return true;
    }

    @Override
    public void setViewManager(GVRViewManager viewManager) {
        daydreamViewManager = (DaydreamViewManager) viewManager;
    }

    @Override
    public VrAppSettings makeVrAppSettings() {
        final VrAppSettings settings = new VrAppSettings();
        final VrAppSettings.EyeBufferParams params = settings.getEyeBufferParams();
        params.setResolutionHeight(VrAppSettings.DEFAULT_FBO_RESOLUTION);
        params.setResolutionWidth(VrAppSettings.DEFAULT_FBO_RESOLUTION);
        return settings;
    }

    @Override
    public void setCameraRig(GVRCameraRig cameraRig) {
        if (daydreamViewManager != null) {
            daydreamViewManager.setCameraRig(cameraRig);
        }
    }

    @Override
    public void onUndock() {

    }

    @Override
    public void onDock() {

    }

    @Override
    public long getNative() {
        return 0;
    }

    /**
     * The class ignores the perspective camera and attaches a custom left and right camera instead.
     * Daydream uses the glFrustum call to create the projection matrix. Using the custom camera
     * allows us to set the projection matrix from the glFrustum call against the custom camera
     * using the set_projection_matrix call in the native renderer.
     */
    static class DaydreamCameraRig extends GVRCameraRig {
        protected DaydreamCameraRig(GVRContext gvrContext) {
            super(gvrContext);
        }

        @Override
        public void attachLeftCamera(GVRCamera camera) {
            GVRCamera leftCamera = new GVRCustomCamera(getGVRContext());
            leftCamera.setRenderMask(GVRRenderData.GVRRenderMaskBit.Left);
            super.attachLeftCamera(leftCamera);
        }

        @Override
        public void attachRightCamera(GVRCamera camera) {
            GVRCamera rightCamera = new GVRCustomCamera(getGVRContext());
            rightCamera.setRenderMask(GVRRenderData.GVRRenderMaskBit.Right);
            super.attachRightCamera(rightCamera);
        }
    }
}
