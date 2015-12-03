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
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSharedTexture;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.plugins.widget.GVRWidgetSceneObjectMeshInfo;
import org.gearvrf.scene_objects.view.GVRView;

import android.graphics.SurfaceTexture;
import android.view.Surface;

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
		float[] vertices = { mMeshInfo.topLeftx, mMeshInfo.bottomrighty,
				mMeshInfo.z,

				mMeshInfo.bottomrightx, mMeshInfo.bottomrighty, mMeshInfo.z,

				mMeshInfo.topLeftx, mMeshInfo.topLefty, mMeshInfo.z,

				mMeshInfo.bottomrightx, mMeshInfo.topLefty, mMeshInfo.z };

		float[] textcoords = {
				(float) mMeshInfo.mTopLeftOfView[0] / (float) mWidth,
				(float) mMeshInfo.mBottomRightOfView[1] / (float) mHeight,

				(float) mMeshInfo.mBottomRightOfView[0] / (float) mWidth,
				(float) mMeshInfo.mBottomRightOfView[1] / (float) mHeight,

				(float) mMeshInfo.mTopLeftOfView[0] / (float) mWidth,
				(float) mMeshInfo.mTopLeftOfView[1] / (float) mHeight,

				(float) mMeshInfo.mBottomRightOfView[0] / (float) mWidth,
				(float) mMeshInfo.mTopLeftOfView[1] / (float) mHeight };

		mesh.setVertices(vertices);
		mesh.setNormals(SIMPLE_NORMALS);
		mesh.setTexCoords(textcoords);
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
