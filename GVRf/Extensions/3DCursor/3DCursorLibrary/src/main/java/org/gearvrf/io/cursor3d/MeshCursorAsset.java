/*
 * Copyright 2016 Samsung Electronics Co., LTD
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

package org.gearvrf.io.cursor3d;

import android.util.SparseArray;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterial.GVRShaderType.Texture;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSwitch;
import org.gearvrf.GVRTexture;
import org.gearvrf.utility.Log;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * This class allows a mesh and a texture to be set on a {@link Cursor object}.
 */
class MeshCursorAsset extends CursorAsset {
    private static final String TAG = MeshCursorAsset.class.getSimpleName();
    private static final int OVERLAY_RENDER_ORDER = 100000;
    private GVRTexture texture;
    private GVRMesh mesh;
    private float x;
    private float y;
    protected SparseArray<GVRSceneObject> sceneObjectArray;

    MeshCursorAsset(GVRContext context, CursorType type, Action action, String texName) {
        this(context, type, action, null, texName);
    }

    MeshCursorAsset(GVRContext context, CursorType type, Action action, String meshName, String
            texName)
    {
        super(context, type, action);
        sceneObjectArray = new SparseArray<GVRSceneObject>();

        if (meshName != null)
        {
            try
            {
                mesh = context.getAssetLoader().loadMesh(new GVRAndroidResource(context, meshName));
            }
            catch (IOException e)
            {
                throw new IllegalArgumentException("Error loading mesh");
            }
        }
        if (texName != null)
        {
            try
            {
                texture = context.getAssetLoader().loadTexture(
                        new GVRAndroidResource(context, texName));
            }
            catch (IOException e)
            {
                throw new IllegalArgumentException("Error loading Texture");
            }
        }
    }


    void setQuadMesh(float x, float y) {
        this.x = x;
        this.y = y;
        mesh = new GVRMesh(context, "float3 a_position float2 a_texcoord");
        mesh.createQuad(x, y);
    }

    @Override
    void load(Cursor cursor)
    {
        Integer key = cursor.getId();
        GVRSceneObject assetSceneObject = sceneObjectArray.get(key);
        GVRRenderData renderData = null;

        if (assetSceneObject == null)
        {
            assetSceneObject = new GVRSceneObject(context);
            assetSceneObject.setName( getAction().toString() + key.toString());
            assetSceneObject.setEnable(false);
            renderData = new GVRRenderData(context);
            renderData.setMaterial(new GVRMaterial(context, Texture.ID));

            if (cursorType == CursorType.LASER)
            {
                renderData.setDepthTest(false);
                renderData.setAlphaBlend(true);
                renderData.setRenderingOrder(OVERLAY_RENDER_ORDER);
            }
            assetSceneObject.attachRenderData(renderData);
            sceneObjectArray.append(key, assetSceneObject);
        }
        renderData = assetSceneObject.getRenderData();
        if (mesh != null)
        {
            renderData.setMesh(mesh);
        }
        if (texture != null)
        {
            renderData.getMaterial().setMainTexture(texture);
        }
        cursor.addChildObject(assetSceneObject);
    }

    @Override
    void unload(Cursor cursor)
    {
        int key = cursor.getId();

        GVRSceneObject assetSceneObject = sceneObjectArray.get(key);
        cursor.removeChildObject(assetSceneObject);
        sceneObjectArray.remove(key);
        // check if there are cursors still using the texture
        if (sceneObjectArray.size() == 0)
        {
            texture = null;
        }
    }

    void set(Cursor cursor)
    {
        super.set(cursor);
        final GVRSceneObject assetSceneObject = sceneObjectArray.get(cursor.getId());
        if (assetSceneObject == null)
        {
            Log.e(TAG, "Render data not found, should not happen");
            return;
        }
        assetSceneObject.setEnable(true);
    }

    /**
     * Use the reset method to remove this asset from the given {@link GVRSceneObject}.
     *
     * @param cursor the {@link GVRSceneObject}  for the behavior to be removed
     */

    void reset(Cursor cursor)
    {
        super.reset(cursor);
        GVRSceneObject assetSceneObject = sceneObjectArray.get(cursor.getId());
        assetSceneObject.setEnable(false);
    }

    float getX() {
        return x;
    }

    float getY() {
        return y;
    }
}