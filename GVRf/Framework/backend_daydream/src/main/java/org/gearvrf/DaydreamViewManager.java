
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

import android.content.Context;
import android.view.KeyEvent;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import org.gearvrf.utility.VrAppSettings;

import javax.microedition.khronos.egl.EGLConfig;

class DaydreamViewManager extends GVRViewManager {
    private static final String TAG = DaydreamViewManager.class.getSimpleName();
    private final float[] headTransform;

    DaydreamViewManager(final GVRActivity gvrActivity, GVRScript gvrScript) {
        super(gvrActivity, gvrScript);
        GvrView gvrView = new GoogleVRView(gvrActivity, this, null);
        gvrActivity.setContentView(gvrView);
        headTransform = new float[4];
    }

    public void onNewFrame(HeadTransform headTransform) {
        headTransform.getQuaternion(this.headTransform, 0);
        mMainScene.getMainCameraRig().getHeadTransform().setRotation(this.headTransform[3],
                this.headTransform[0], this.headTransform[1], this.headTransform[2]);

        updateSensoredScene();
    }

    public void onDrawEye(Eye eye) {
        if (eye.getType() == Eye.Type.LEFT) {
            renderCamera(mMainScene, mMainScene.getMainCameraRig().getLeftCamera(), mRenderBundle);
        } else if (eye.getType() == Eye.Type.RIGHT) {
            renderCamera(mMainScene, mMainScene.getMainCameraRig().getRightCamera(), mRenderBundle);
        }
    }

    private static class GoogleVRViewRenderer implements GvrView.StereoRenderer {
        private DaydreamViewManager mViewManager = null;

        public GoogleVRViewRenderer(DaydreamViewManager viewManager) {
            mViewManager = viewManager;
        }

        public void setViewManager(DaydreamViewManager viewManager) {
            mViewManager = viewManager;
        }

        @Override
        public void onSurfaceChanged(int width, int height) {
        }

        @Override
        public void onSurfaceCreated(EGLConfig config) {
            mViewManager.onSurfaceCreated();
        }

        @Override
        public void onDrawEye(Eye eye) {
            mViewManager.onDrawEye(eye);
        }

        @Override
        public void onNewFrame(HeadTransform headTransform) {
            //todo move onnewframe to impl in viewmgr; run it before before draw
            mViewManager.beforeDrawEyes();
            mViewManager.onNewFrame(headTransform);
        }

        @Override
        public void onFinishFrame(Viewport viewport) {
            mViewManager.afterDrawEyes();
        }

        @Override
        public void onRendererShutdown() {
        }
    }

    private static class GoogleVRView extends GvrView {
        public GoogleVRView(Context context) {
            super(context);
        }

        public GoogleVRView(Context context, final DaydreamViewManager viewManager,
                            GoogleVRViewRenderer renderer) {
            super(context);
             setEGLConfigChooser(8, 8, 8, 8, 16, 8);

            if (renderer != null) {
                renderer.setViewManager(viewManager);
                setRenderer(renderer);
            } else {
                setRenderer(new GoogleVRViewRenderer(viewManager));
            }
            setTransitionViewEnabled(true);
            setOnCardboardBackButtonListener(
                    new Runnable() {
                        @Override
                        public void run() {
                            viewManager.getActivity().onBackPressed();
                        }
                    });
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            return super.onKeyDown(keyCode, event);
        }
    }
}
