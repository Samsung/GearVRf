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

import java.util.concurrent.Future;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

public class Cube extends GVRSceneObject {

    public Cube(GVRContext gvrContext, String name,
            CustomShaderManager shaderManager) {
        super(gvrContext);

        Future<GVRTexture> texture = gvrContext.loadFutureTexture(
                new GVRAndroidResource(gvrContext, R.raw.cube_texture));
        GVRMaterial material = new GVRMaterial(gvrContext,
                shaderManager.getShaderId());
        setColor(material, 0.7f, 0.7f, 0.7f);
        material.setTexture(CustomShaderManager.TEXTURE_KEY, texture);
        Future<GVRMesh> mesh = gvrContext
                .loadFutureMesh(new GVRAndroidResource(gvrContext, R.raw.cube));
        GVRRenderData renderData = new GVRRenderData(gvrContext);
        renderData.setMesh(mesh);
        renderData.setMaterial(material);
        attachRenderData(renderData);
        setName(name);
    }

    void setRed() {
        setColor(1.0f, 0.0f, 0.0f);
    }

    void setGreen() {
        setColor(0.0f, 1.0f, 0.0f);
    }

    void setGrey() {
        setColor(0.7f, 0.7f, 0.7f);
    }

    void setColor(float r, float g, float b) {
        GVRMaterial material = getRenderData().getMaterial();
        material.setVec3(CustomShaderManager.COLOR_KEY, r, g, b);
    }

    void setColor(GVRMaterial material, float r, float g, float b) {
        material.setVec3(CustomShaderManager.COLOR_KEY, r, g, b);
    }

}
