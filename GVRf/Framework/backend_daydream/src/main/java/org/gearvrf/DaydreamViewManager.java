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

import android.opengl.GLSurfaceView;
import android.view.WindowManager;

import com.google.vr.ndk.base.AndroidCompat;
import com.google.vr.ndk.base.GvrLayout;

class DaydreamViewManager extends GVRViewManager {
    private static final String TAG = DaydreamViewManager.class.getSimpleName();
    private GvrLayout gvrLayout;
    private DaydreamRenderer renderer;
    private GLSurfaceView surfaceView;
    private GVRCameraRig cameraRig;
    private boolean sensoredSceneUpdated = false;
    private GearCursorController mGearController;
    private DayDreamControllerReader mControllerReader;

    // This is done on the GL thread because refreshViewerProfile isn't thread-safe.
    private final Runnable refreshViewerProfileRunnable =
            new Runnable() {
                @Override
                public void run() {
                    gvrLayout.getGvrApi().refreshViewerProfile();
                }
            };

    DaydreamViewManager(final GVRActivity gvrActivity, GVRMain gvrMain, boolean useMultiview) {
        super(gvrActivity, gvrMain, useMultiview);
        // Initialize GvrLayout and the native renderer.
        gvrLayout = new GvrLayout(gvrActivity);

        surfaceView = new GLSurfaceView(gvrActivity);
        surfaceView.setEGLContextClientVersion(3);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 24, 8);
        surfaceView.setPreserveEGLContextOnPause(true);

        renderer = new DaydreamRenderer(this, gvrLayout.getGvrApi().getNativeGvrContext());
        surfaceView.setRenderer(renderer);

        gvrLayout.setPresentationView(surfaceView);

        // Add the GvrLayout to the View hierarchy.
        gvrActivity.setContentView(gvrLayout);

        // Enable scan line racing.
        if (gvrLayout.setAsyncReprojectionEnabled(true)) {
            // Scanline racing decouples the app framerate from the display framerate,
            // allowing immersive interaction even at the throttled clockrates set by
            // sustained performance mode.
            AndroidCompat.setSustainedPerformanceMode(gvrActivity, true);
        }

        // Enable VR Mode.
        AndroidCompat.setVrModeEnabled(gvrActivity, true);

        // Prevent screen from dimming/locking.
        gvrActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mControllerReader = new DayDreamControllerReader(gvrActivity);
    }

    public long getNativeRenderer(){
        return renderer.getNativeDaydreamRenderer();
    }
    @Override
    void onResume() {
        super.onResume();
        renderer.onResume();
        gvrLayout.onResume();
        surfaceView.onResume();
        surfaceView.queueEvent(refreshViewerProfileRunnable);
    }

    @Override
    void onSurfaceCreated()
    {
        super.onSurfaceCreated();
        mGearController = mInputManager.getGearController();
        if (mGearController != null)
        {
            mGearController.attachReader(mControllerReader);
        }
    }

    @Override
    void onPause() {
        super.onPause();
        renderer.onPause();
        gvrLayout.onPause();
        surfaceView.onPause();
    }

    @Override
    void onDestroy() {
        super.onDestroy();
        gvrLayout.shutdown();
        renderer.onDestroy();
        mGearController = null;
    }
    void onDrawFrame(){
        mGearController.onDrawFrame();
    }
    void onDrawEye(int eye) {
        if (cameraRig == null) {
            return;
        }

        if (!sensoredSceneUpdated) {
            sensoredSceneUpdated = updateSensoredScene();
        }
        if (eye == 0) {
            captureCenterEye();
            capture3DScreenShot();

            renderCamera(mMainScene, cameraRig.getLeftCamera(), mRenderBundle, false);
            captureLeftEye(0);
        } else {
            renderCamera(mMainScene, cameraRig.getRightCamera(), mRenderBundle, false);
            captureRightEye(0);
        }
        captureFinish();
    }

    void setCameraRig(GVRCameraRig cameraRig) {
        this.cameraRig = cameraRig;
        renderer.setCameraRig(cameraRig);
        sensoredSceneUpdated = false;
    }
}
