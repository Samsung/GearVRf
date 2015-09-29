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

package org.gearvrf.io;

import java.util.Random;
import java.util.concurrent.Future;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

public class Cursor extends GVRSceneObject {

    private static Random random = new Random();

    public Cursor(GVRContext gvrContext, CustomShaderManager shaderManager) {
        super(gvrContext);
        Future<GVRTexture> texture = gvrContext.loadFutureTexture(
                new GVRAndroidResource(gvrContext, R.raw.earthmap1k));
        GVRMaterial material = new GVRMaterial(gvrContext,
                shaderManager.getShaderId());
        // stay away from the darker colors for the cursor
        float r = random.nextFloat() / 2.0f + 0.5f;
        float g = random.nextFloat() / 2.0f + 0.5f;
        float b = random.nextFloat() / 2.0f + 0.5f;
        material.setVec3(CustomShaderManager.COLOR_KEY, r, g, b);
        material.setTexture(CustomShaderManager.TEXTURE_KEY, texture);
        Future<GVRMesh> futureCursorMesh = gvrContext.loadFutureMesh(
                new GVRAndroidResource(gvrContext, R.raw.cursor));
        GVRRenderData cursorRenderData = new GVRRenderData(gvrContext);
        cursorRenderData.setMesh(futureCursorMesh);
        cursorRenderData.setMaterial(material);
        attachRenderData(cursorRenderData);
    }
}
