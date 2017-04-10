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

package org.gearvrf.widgetplugin;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSharedTexture;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.scene_objects.view.GVRView;

import android.graphics.SurfaceTexture;
import android.view.Surface;


/**
 * GVRWidgetSceneObject shows the GVRwidget widgets are created from LibGDX
 * GlSurfaceView which is created by app as follows
 * <pre>
 *     <code>
 * 0__________300_________600
 * |          |           |
 * |          |           |
 * |          |     2     |
 * |     1    |___________|250
 * |          |           |
 * |          |           |
 * |          |     3     |
 * |__________|___________|
 *  500
 *     </code>
 * </pre>
 *  above is parent GLSurfaceView of width 600 pixel and height 500 pixel, which has to be broken
 *  up in 3 GVRWidgetSceneObjects as laid out. the first Scene object would display rectangle
 *  created by 1 to 300 pixels horizontally and 1 to 500 pixels vertically the second scene
 *  object would display rectangle created by 301 to 600 pixels horizontally and 1 to 250 pixels
 *  vertically the third scene object would display rectangle created by 301 to 600 pixels
 *  horizontally and 251 to 500 pixels vertically scene objects will be rendered with texture of
 *  this view by assigning appropriate texture coordinates.
 * 
 */
public class GVRWidgetSceneObject extends GVRSceneObject {

    private int mWidth;
    private int mHeight;

    private static final float[] SIMPLE_NORMALS = { 0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f };

    private static final char[] SIMPLE_INDICES = { 0, 1, 2, 2, 1, 3 };

    private GVRWidgetSceneObjectMeshInfo mMeshInfo;

    /**
     * Shows GVRF UI widgets (Libgdx scene2d) {@linkplain GVRWidgetSceneObject
     * scene object}
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param mesh
     *            a {@link GVRMesh} - see
     *            {@link GVRContext#loadMesh(org.gearvrf.GVRAndroidResource)}
     *            and {@link GVRContext#createQuad(float, float)}
     * @param int Libgdx Texture ID
     */
    public GVRWidgetSceneObject(GVRContext gvrContext, GVRMesh mesh, int texid) {
        super(gvrContext, mesh);

        GVRMaterial material = new GVRMaterial(gvrContext,
                GVRShaderType.UnlitFBO.ID);
        if (texid != 0) {
            material.setMainTexture(new GVRSharedTexture(gvrContext, texid));
        }
        getRenderData().setMaterial(material);

    }

    /**
     * Shows GVRF UI widgets (Libgdx scene2d) {@linkplain GVRWidgetSceneObject
     * scene object}
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     * @param GVRWidgetSceneObjectMeshInfo
     *            a {@link GVRWidgetSceneObjectMeshInfo} - see
     * 
     * @param int Libgdx viewWidth
     * @param int Libgdx viewHeight
     */
    public GVRWidgetSceneObject(GVRContext gvrContext, int texid,
            GVRWidgetSceneObjectMeshInfo mesh, int viewWidth, int viewHeight) {
        super(gvrContext);
        mMeshInfo = mesh;
        mWidth = viewWidth;
        mHeight = viewHeight;
        GVRMaterial material = new GVRMaterial(gvrContext,
                GVRShaderType.UnlitFBO.ID);
        if (texid != 0) {
            material.setMainTexture(new GVRSharedTexture(gvrContext, texid));
        }
        createSceneObject(gvrContext, material);
    }

    private void createSceneObject(GVRContext gvrContext, GVRMaterial mat) {
        GVRMesh mesh = new GVRMesh(gvrContext);
        float[] vertices = { mMeshInfo.mTopLeftX, mMeshInfo.mBottomRightY,
                mMeshInfo.mZ,

                mMeshInfo.mBottomRightX, mMeshInfo.mBottomRightY, mMeshInfo.mZ,

                mMeshInfo.mTopLeftX, mMeshInfo.mTopLeftY, mMeshInfo.mZ,

                mMeshInfo.mBottomRightX, mMeshInfo.mTopLeftY, mMeshInfo.mZ };

        float[] textCoords = {
                (float) mMeshInfo.mTopLeftViewCoords[0] / (float) mWidth,
                (float) mMeshInfo.mBottomRightViewCoords[1] / (float) mHeight,

                (float) mMeshInfo.mBottomRightViewCoords[0] / (float) mWidth,
                (float) mMeshInfo.mBottomRightViewCoords[1] / (float) mHeight,

                (float) mMeshInfo.mTopLeftViewCoords[0] / (float) mWidth,
                (float) mMeshInfo.mTopLeftViewCoords[1] / (float) mHeight,

                (float) mMeshInfo.mBottomRightViewCoords[0] / (float) mWidth,
                (float) mMeshInfo.mTopLeftViewCoords[1] / (float) mHeight };

        mesh.setVertices(vertices);
        mesh.setNormals(SIMPLE_NORMALS);
        mesh.setTexCoords(textCoords);
        mesh.setIndices(SIMPLE_INDICES);

        GVRRenderData renderData = new GVRRenderData(gvrContext);
        renderData.setMaterial(mat);
        attachRenderData(renderData);
        renderData.setMesh(mesh);
    }

    public GVRWidgetSceneObjectMeshInfo getmeshInfo() {
        return mMeshInfo;
    }
}
