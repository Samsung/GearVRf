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

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRMaterial.GVRShaderType;

import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.webkit.WebView;

/**
 * {@linkplain GVRSceneObject Scene object} that shows a web page, using the
 * Android {@link WebView}.
 */

@Deprecated
public class GVRWebViewSceneObject extends GVRSceneObject implements
        GVRDrawFrameListener {
    private static final int REFRESH_INTERVAL = 30; // frames

    private final Surface mSurface;
    private final SurfaceTexture mSurfaceTexture;
    private final WebView mWebView;
    private int mCount = 0;

    /**
     * Shows a web page on a {@linkplain GVRSceneObject scene object} with an
     * arbitrarily complex geometry.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param mesh
     *            a {@link GVRMesh} - see
     *            {@link GVRContext#loadMesh(org.gearvrf.GVRAndroidResource)}
     *            and {@link GVRContext#createQuad(float, float)}
     * @param webView
     *            an Android {@link WebView}
     */
    public GVRWebViewSceneObject(GVRContext gvrContext, GVRMesh mesh,
            WebView webView) {
        super(gvrContext, mesh);
        mWebView = webView;
        gvrContext.registerDrawFrameListener(this);
        GVRTexture texture = new GVRExternalTexture(gvrContext);
        GVRMaterial material = new GVRMaterial(gvrContext, GVRShaderType.OES.ID);
        material.setMainTexture(texture);
        getRenderData().setMaterial(material);

        mSurfaceTexture = new SurfaceTexture(texture.getId());
        mSurface = new Surface(mSurfaceTexture);
        mSurfaceTexture.setDefaultBufferSize(mWebView.getWidth(),
                mWebView.getHeight());
    }

    /**
     * Shows a web page in a 2D, rectangular {@linkplain GVRSceneObject scene
     * object.}
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param width
     *            the rectangle's width
     * @param height
     *            the rectangle's height
     * @param webView
     *            a {@link WebView}
     */
    public GVRWebViewSceneObject(GVRContext gvrContext, float width,
            float height, WebView webView) {
        this(gvrContext, gvrContext.createQuad(width, height), webView);
    }

    @Override
    public void onDrawFrame(float frameTime) {
        if (++mCount > REFRESH_INTERVAL) {
            refresh();
            mCount = 0;
        }
    }

    /** Draws the {@link WebView} onto {@link #mSurfaceTexture} */
    private void refresh() {
        try {
            Canvas canvas = mSurface.lockCanvas(null);
            mWebView.draw(canvas);
            mSurface.unlockCanvasAndPost(canvas);
        } catch (Surface.OutOfResourcesException t) {
            Log.e("GVRWebBoardObject", "lockCanvas failed");
        }
        mSurfaceTexture.updateTexImage();
    }
}
