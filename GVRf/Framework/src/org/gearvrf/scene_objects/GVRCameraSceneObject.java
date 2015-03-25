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

/**
 * A {@linkplain GVRSceneObject scene object} that shows live video from one of
 * the device's cameras
 */
public class GVRCameraSceneObject extends GVRSceneObject implements
        GVRDrawFrameListener {
    private final SurfaceTexture mSurfaceTexture;
    private boolean mPaused = false;

    /**
     * Create a {@linkplain GVRSceneObject scene object} (with arbitrarily
     * complex geometry) that shows live video from one of the device's cameras
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param mesh
     *            an arbitrarily complex {@link GVRMesh} object - see
     *            {@link GVRContext#loadMesh(String)} and
     *            {@link GVRContext#createQuad(float, float)}
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

        mSurfaceTexture = new SurfaceTexture(texture.getId());
        try {
            camera.setPreviewTexture(mSurfaceTexture);
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
}
