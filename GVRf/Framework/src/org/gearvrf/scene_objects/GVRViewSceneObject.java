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

import java.lang.ref.WeakReference;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.view.GVRView;

import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.view.Surface;

/**
 * This class represents a {@linkplain GVRSceneObject Scene object} that shows a {@link View}
 * into the scene with an arbitrarily complex geometry.
 * See {@link GVRView}
 */
public class GVRViewSceneObject extends GVRSceneObject {

    private final Surface mSurface;
    private final SurfaceTexture mSurfaceTexture;
    private final GVRView mView;

    /**
     * Shows any view into the {@linkplain GVRViewSceneObject scene object} with an arbitrarily complex
     * geometry.
     * 
     * @param gvrContext current {@link GVRContext}
     * @param gvrView The {@link GVRView} to be shown.
     * @param mesh a {@link GVRMesh} - see
     *            {@link GVRContext#loadMesh(org.gearvrf.GVRAndroidResource)} and
     *            {@link GVRContext#createQuad(float, float)}
     */
    public GVRViewSceneObject(GVRContext gvrContext, GVRView gvrView, GVRMesh mesh) {
        super(gvrContext, mesh);

        GVRTexture texture = new GVRExternalTexture(gvrContext);

        // TODO: Shader type maybe defined by some GVRView.getShaderType()
        // according to view type
        GVRMaterial material = new GVRMaterial(gvrContext, GVRShaderType.OES.ID);
        material.setMainTexture(texture);
        getRenderData().setMaterial(material);

        mSurfaceTexture = new SurfaceTexture(texture.getId());
        mSurface = new Surface(mSurfaceTexture);
        mSurfaceTexture.setDefaultBufferSize(gvrView.getView().getWidth(),
                gvrView.getView().getHeight());

        gvrContext.registerDrawFrameListener(new GVRDrawFrameListenerImpl(gvrContext, mSurfaceTexture));
        gvrView.setSceneObject(this);

        gvrView.getView().postInvalidate();
        mView = gvrView;
    }

    public GVRView getView() {
        return mView;
    }

    /**
     * Shows view in a 2D, rectangular {@linkplain GVRViewSceneObject scene object.}
     * 
     * @param gvrContext current {@link GVRContext}
     * @param gvrView The {@link GVRView} to be shown.
     * @param width the rectangle's width
     * @param height the rectangle's height
     */
    public GVRViewSceneObject(GVRContext gvrContext, GVRView gvrView,
            float width, float height) {
        this(gvrContext, gvrView, gvrContext.createQuad(width, height));
    }

    /**
     * Gets a Android {@link Canvas} for drawing into this {@link GVRViewSceneObject Scene object}.
     * After drawing into the provided Android {@link Canvas}, the caller must invoke {@linkplain
     * GVRViewSceneObject#unlockCanvasAndPost(Android {@link Canvas}) to post the new contents to the scene
     * object. See - Android {@link Surface#lockCanvas(android.graphics.Rect)}
     */
    public Canvas lockCanvas() {
        return mSurface.lockCanvas(null);
    }

    /**
     * Posts the new contents of the Android {@link Canvas} to the scene object and releases the
     * Android {@link Canvas}.
     *
     * @param canvas The canvas previously obtained from {@link GVRViewSceneObject#lockCanvas()} See
     *            - Android {@link Surface#unlockCanvasAndPost(Canvas)}
     */
    public void unlockCanvasAndPost(Canvas canvas) {
        mSurface.unlockCanvasAndPost(canvas);
    }

    private static final class GVRDrawFrameListenerImpl implements GVRDrawFrameListener {
        GVRDrawFrameListenerImpl(final GVRContext gvrContext, final SurfaceTexture surfaceTexture) {
            mSurfaceTextureRef = new WeakReference<SurfaceTexture>(surfaceTexture);
            mGvrContext = gvrContext;
        }

        @Override
        public void onDrawFrame(float frameTime) {
            final SurfaceTexture surfaceTexture = mSurfaceTextureRef.get();
            if (null != surfaceTexture) {
                surfaceTexture.updateTexImage();
            } else {
                mGvrContext.unregisterDrawFrameListener(this);
            }
        }

        private final WeakReference<SurfaceTexture> mSurfaceTextureRef;
        private final GVRContext mGvrContext;
    }
}
