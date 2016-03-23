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

import java.io.IOException;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRMaterial.GVRShaderType;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import org.gearvrf.utility.Log;

/**
 * A {@linkplain GVRSceneObject scene object} that shows live video from one of
 * the device's cameras
 */
public class GVRCameraSceneObject extends GVRSceneObject implements
        GVRDrawFrameListener {
    
    private static String TAG = GVRSceneObject.class.getSimpleName();
    private final SurfaceTexture mSurfaceTexture;
    private boolean mPaused = false;
    private Camera camera;
    private GVRContext gvrContext;
    private boolean cameraSetUpStatus;

    /**
     * Create a {@linkplain GVRSceneObject scene object} (with arbitrarily
     * complex geometry) that shows live video from one of the device's cameras
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param mesh
     *            an arbitrarily complex {@link GVRMesh} object - see
     *            {@link GVRContext#loadMesh(org.gearvrf.GVRAndroidResource)}
     *            and {@link GVRContext#createQuad(float, float)}
     * @param camera
     *            an Android {@link Camera}. <em>Note</em>: this constructor
     *            calls {@link Camera#setPreviewTexture(SurfaceTexture)} so you
     *            should be sure to call it before you call
     *            {@link Camera#startPreview()}.
     */
    public GVRCameraSceneObject(GVRContext gvrContext, GVRMesh mesh,
            Camera camera) {
        super(gvrContext, mesh);
        gvrContext.registerDrawFrameListener(this);
        GVRTexture texture = new GVRExternalTexture(gvrContext);
        GVRMaterial material = new GVRMaterial(gvrContext, GVRShaderType.OES.ID);
        material.setMainTexture(texture);
        getRenderData().setMaterial(material);

        this.gvrContext = gvrContext;
        this.camera = camera;
        mSurfaceTexture = new SurfaceTexture(texture.getId());
        try {
            this.camera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a 2D, rectangular {@linkplain GVRSceneObject scene object} that
     * shows live video from one of the device's cameras
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param width
     *            the scene rectangle's width
     * @param height
     *            the rectangle's height
     * @param camera
     *            an Android {@link Camera}. <em>Note</em>: this constructor
     *            calls {@link Camera#setPreviewTexture(SurfaceTexture)} so you
     *            should be sure to call it before you call
     *            {@link Camera#startPreview()}.
     */
    public GVRCameraSceneObject(GVRContext gvrContext, float width,
            float height, Camera camera) {
        this(gvrContext, gvrContext.createQuad(width, height), camera);
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

    @Override
    public void onDrawFrame(float drawTime) {
        if (!mPaused) {
            mSurfaceTexture.updateTexImage();
        }
    }

    
    /**
     * Configure high fps settings in the camera for VR mode
     * 
     * @param fpsMode
     *            integer indicating the desired fps: 0 means 30 fps, 1 means 60
     *            fps, and 2 means 120 fps. Any other value is invalid.
     * @return  A boolean indicating the status of the method call. It may be false due 
     *          to multiple reasons including: 1) supplying invalid fpsMode as the input
     *          parameter, 2) VR mode not supported.
     */
    public boolean setUpCameraForVrMode(final int fpsMode) {
        
        cameraSetUpStatus = false;

        if (fpsMode < 0 || fpsMode > 2) {
            Log.e(TAG,
                    "Invalid fpsMode: %d. It can only take values 0, 1, or 2.", fpsMode);
        } else {
            gvrContext.getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
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
            });
        }

        return cameraSetUpStatus;
    }
}
