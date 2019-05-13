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

package org.gearvrf.scene_objects;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;

import org.gearvrf.GVRContext;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRExternalImage;
import org.gearvrf.GVRImage;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.utility.Log;

import java.io.IOException;

/**
 * A {@linkplain GVRSceneObject scene object} that shows live video from one of
 * the device's cameras
 */
public class GVRCameraSceneObject extends GVRSceneObject {
    private static String TAG = GVRCameraSceneObject.class.getSimpleName();
    private final SurfaceTexture mSurfaceTexture;
    private boolean mPaused = false;
    private Camera camera;
    private GVRContext gvrContext;
    private boolean cameraSetUpStatus;
    private int fpsMode = -1;
    private boolean isCameraOpen = false;
    private CameraActivityEvents cameraActivityEvents;

    /**
     * Create a {@linkplain GVRSceneObject scene object} (with arbitrarily
     * complex geometry) that shows live video from one of the device's cameras
     *
     * @param gvrContext current {@link GVRContext}
     * @param mesh       an arbitrarily complex {@link GVRMesh} object - see
     *                   {@link GVRContext#loadMesh(org.gearvrf.GVRAndroidResource)}
     *                   and {@link GVRContext#createQuad(float, float)}
     * @param camera     an Android {@link Camera}. <em>Note</em>: this constructor
     *                   calls {@link Camera#setPreviewTexture(SurfaceTexture)} so you
     *                   should be sure to call it before you call
     *                   {@link Camera#startPreview()}.
     * @deprecated This call does not ensure the activity lifecycle is correctly
     * handled by the {@link GVRCameraSceneObject}. Use
     * {@link #GVRCameraSceneObject(GVRContext, GVRMesh)} instead.
     */
    public GVRCameraSceneObject(GVRContext gvrContext, GVRMesh mesh,
                                Camera camera) {
        super(gvrContext, mesh);
        final GVRImage image = new GVRExternalImage(gvrContext);
        final GVRTexture texture = new GVRTexture(image);
        GVRMaterial material = new GVRMaterial(gvrContext, GVRShaderType.OES.ID);
        material.setMainTexture(texture);
        getRenderData().setMaterial(material);

        this.gvrContext = gvrContext;
        this.camera = camera;
        isCameraOpen = true;
        mSurfaceTexture = new SurfaceTexture(texture.getId());
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            Runnable onFrameAvailableGLCallback = new Runnable() {
                @Override
                public void run() {
                    mSurfaceTexture.updateTexImage();
                }
            };

            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                GVRCameraSceneObject.this.gvrContext.runOnGlThread(onFrameAvailableGLCallback);
            }
        });

        try {
            this.camera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a {@linkplain GVRSceneObject scene object} (with arbitrarily
     * complex geometry) that shows live video from one of the device's cameras
     *
     * @param gvrContext current {@link GVRContext}
     * @param mesh       an arbitrarily complex {@link GVRMesh} object - see
     *                   {@link org.gearvrf.GVRAssetLoader#loadMesh(org.gearvrf.GVRAndroidResource)}
     *                   and {@link GVRContext#createQuad(float, float)}
     * @throws GVRCameraAccessException returns this exception when the camera cannot be
     *                                  initialized correctly.
     */
    public GVRCameraSceneObject(GVRContext gvrContext, GVRMesh mesh) throws
            GVRCameraAccessException {
        super(gvrContext, mesh);

        final GVRImage image = new GVRExternalImage(gvrContext);
        final GVRTexture texture = new GVRTexture(image);
        GVRMaterial material = new GVRMaterial(gvrContext, GVRShaderType.OES.ID);
        material.setMainTexture(texture);
        getRenderData().setMaterial(material);
        this.gvrContext = gvrContext;

        mSurfaceTexture = new SurfaceTexture(texture.getId());
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            Runnable onFrameAvailableGLCallback = new Runnable() {
                @Override
                public void run() {
                    mSurfaceTexture.updateTexImage();
                }
            };

            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                GVRCameraSceneObject.this.gvrContext.runOnGlThread(onFrameAvailableGLCallback);
            }
        });

        if (!openCamera()) {
            Log.e(TAG, "Cannot open the camera");
            throw new GVRCameraAccessException("Cannot open the camera");
        }

        cameraActivityEvents = new CameraActivityEvents();
        gvrContext.getApplication().getEventReceiver().addListener(cameraActivityEvents);
    }

    /**
     * Create a 2D, rectangular {@linkplain GVRSceneObject scene object} that
     * shows live video from one of the device's cameras
     *
     * @param gvrContext current {@link GVRContext}
     * @param width      the scene rectangle's width
     * @param height     the rectangle's height
     * @param camera     an Android {@link Camera}. <em>Note</em>: this constructor
     *                   calls {@link Camera#setPreviewTexture(SurfaceTexture)} so you
     *                   should be sure to call it before you call
     *                   {@link Camera#startPreview()}.
     * @deprecated This call does not ensure the activity lifecycle is correctly
     * handled by the {@link GVRCameraSceneObject}. Use
     * {@link #GVRCameraSceneObject(GVRContext, float, float)} instead.
     */
    public GVRCameraSceneObject(GVRContext gvrContext, float width,
                                float height, Camera camera) {
        this(gvrContext, gvrContext.createQuad(width, height), camera);
    }

    /**
     * Create a 2D, rectangular {@linkplain GVRSceneObject scene object} that
     * shows live video from one of the device's cameras.
     *
     * @param gvrContext current {@link GVRContext}
     * @param width      the scene rectangle's width
     * @param height     the rectangle's height
     *
     * @throws GVRCameraAccessException this exception is returned when the camera cannot be opened.
     */
    public GVRCameraSceneObject(GVRContext gvrContext, float width,
                                float height) throws GVRCameraAccessException {
        this(gvrContext, gvrContext.createQuad(width, height));
    }

    private boolean openCamera() {
        if (camera != null) {
            //already open
            return true;
        }

        if (!checkCameraHardware(gvrContext.getActivity())) {
            android.util.Log.d(TAG, "Camera hardware not available.");
            return false;
        }
        try {
            camera = Camera.open();

            if (camera == null) {
                android.util.Log.d(TAG, "Camera not available or is in use");
                return false;
            }
            camera.startPreview();
            camera.setPreviewTexture(mSurfaceTexture);
            isCameraOpen = true;
        } catch (Exception exception) {
            android.util.Log.d(TAG, "Camera not available or is in use");
            return false;
        }

        return true;
    }

    private void closeCamera() {
        if (camera == null) {
            //nothing to do
            return;
        }

        camera.stopPreview();
        camera.release();
        camera = null;
        isCameraOpen = false;
    }

    private class CameraActivityEvents extends GVREventListeners.ActivityEvents {
        @Override
        public void onPause() {
            mPaused = true;
            closeCamera();
        }

        @Override
        public void onResume() {
            if (openCamera()) {
                //restore fpsmode
                setUpCameraForVrMode(fpsMode);
            }
            mPaused = false;
        }
    }

    /**
     * Resumes camera preview
     *
     * <p>
     * Note: {@link #pause()} and {@code resume()} only affect the polling that
     * links the Android {@link Camera} to this {@linkplain GVRSceneObject GVRF
     * scene object:} they have <em>no affect</em> on the underlying
     * {@link Camera} object.
     */
    public void resume() {
        mPaused = false;
    }

    /**
     * Pauses camera preview
     *
     * <p>
     * Note: {@code pause()} and {@link #resume()} only affect the polling that
     * links the Android {@link Camera} to this {@linkplain GVRSceneObject GVRF
     * scene object:} they have <em>no affect</em> on the underlying
     * {@link Camera} object.
     */
    public void pause() {
        mPaused = true;
    }

    /**
     * Close the {@link GVRCameraSceneObject}.
     */
    public void close() {
        closeCamera();
        if(cameraActivityEvents != null){
            gvrContext.getApplication().getEventReceiver().removeListener(cameraActivityEvents);
        }
    }

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA);
    }

    /**
     * Configure high fps settings in the camera for VR mode
     *
     * @param fpsMode integer indicating the desired fps: 0 means 30 fps, 1 means 60
     *                fps, and 2 means 120 fps. Any other value is invalid.
     * @return A boolean indicating the status of the method call. It may be false due
     * to multiple reasons including: 1) supplying invalid fpsMode as the input
     * parameter, 2) VR mode not supported.
     */
    public boolean setUpCameraForVrMode(final int fpsMode) {

        cameraSetUpStatus = false;
        this.fpsMode = fpsMode;

        if (!isCameraOpen) {
            Log.e(TAG, "Camera is not open");
            return false;
        }
        if (fpsMode < 0 || fpsMode > 2) {
            Log.e(TAG,
                    "Invalid fpsMode: %d. It can only take values 0, 1, or 2.", fpsMode);
        } else {
            Parameters params = camera.getParameters();

            // check if the device supports vr mode preview
            if ("true".equalsIgnoreCase(params.get("vrmode-supported"))) {

                Log.v(TAG, "VR Mode supported!");

                // set vr mode
                params.set("vrmode", 1);

                // true if the apps intend to record videos using
                // MediaRecorder
                params.setRecordingHint(true);

                // set preview size
                // params.setPreviewSize(640, 480);

                // set fast-fps-mode: 0 for 30fps, 1 for 60 fps,
                // 2 for 120 fps
                params.set("fast-fps-mode", fpsMode);

                switch (fpsMode) {
                    case 0: // 30 fps
                        params.setPreviewFpsRange(30000, 30000);
                        break;
                    case 1: // 60 fps
                        params.setPreviewFpsRange(60000, 60000);
                        break;
                    case 2: // 120 fps
                        params.setPreviewFpsRange(120000, 120000);
                        break;
                    default:
                }

                // for auto focus
                params.set("focus-mode", "continuous-video");

                params.setVideoStabilization(false);
                if ("true".equalsIgnoreCase(params.get("ois-supported"))) {
                    params.set("ois", "center");
                }

                camera.setParameters(params);
                cameraSetUpStatus = true;
            }
        }

        return cameraSetUpStatus;
    }

    /**
     * This Exception is returned when the {@link GVRCameraSceneObject} cannot be instantiated
     * when the camera is not available.
     */
    public class GVRCameraAccessException extends Exception {
        public GVRCameraAccessException(String message) {
            super(message);
        }
    }
}
