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

package org.gearvrf.cockpit;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.GVRTextureParameters.TextureMagFilterType;
import org.gearvrf.GVRTextureParameters.TextureMinFilterType;
import org.gearvrf.GVRTextureParameters.TextureWrapSType;
import org.gearvrf.GVRTextureParameters.TextureWrapTType;

public class CockpitViewManager extends GVRScript {

    private GVRContext mGVRContext = null;
    private GVRSceneObject mShipSceneObject = null;
    private GVRSceneObject mSpaceSceneObject = null;

    @Override
    public void onInit(GVRContext gvrContext) {

        mGVRContext = gvrContext;
        GVRScene mainScene = mGVRContext.getNextMainScene();

        mainScene.getMainCameraRig().getTransform()
                .setPosition(0.0f, 6.0f, 1.0f);

        GVRMesh shipMesh = mGVRContext.loadMesh(new GVRAndroidResource(
                mGVRContext, R.raw.gvrf_ship_mesh));
        GVRMesh spaceMesh = mGVRContext.loadMesh(new GVRAndroidResource(
                mGVRContext, R.raw.gvrf_space_mesh));

        GVRTextureParameters textureParameters = new GVRTextureParameters();

        if (textureParameters.isAnisotropicSupported()) {
            textureParameters.setAnisotropicValue(12);
        }

        textureParameters.setMagFilterType(TextureMagFilterType.GL_NEAREST);
        textureParameters
                .setMinFilterType(TextureMinFilterType.GL_LINEAR_MIPMAP_NEAREST);
        textureParameters.setWrapSType(TextureWrapSType.GL_REPEAT);
        textureParameters.setWrapTType(TextureWrapTType.GL_REPEAT);

        GVRTexture shipTexture = gvrContext.loadTexture(new GVRAndroidResource(
                mGVRContext, R.drawable.gvrf_ship_png), textureParameters);
        
        
        
        
        // Update texture parameters after creation
        
        textureParameters.setAnisotropicValue(6);
        textureParameters.setMagFilterType(TextureMagFilterType.GL_NEAREST);
        textureParameters.setMinFilterType(TextureMinFilterType.GL_NEAREST);
        textureParameters.setWrapSType(TextureWrapSType.GL_MIRRORED_REPEAT);
        textureParameters.setWrapTType(TextureWrapTType.GL_MIRRORED_REPEAT);
        
        shipTexture.updateTextureParameters(textureParameters);
        
        
        
        
        
        
        
        mShipSceneObject = new GVRSceneObject(gvrContext, shipMesh, shipTexture);
        GVRTexture spaceTexture = gvrContext
                .loadTexture(new GVRAndroidResource(mGVRContext,
                        R.drawable.gvrf_space_png));
        mSpaceSceneObject = new GVRSceneObject(gvrContext, spaceMesh,
                spaceTexture);

        mainScene.addSceneObject(mShipSceneObject);
        mainScene.addSceneObject(mSpaceSceneObject);

    }

    @Override
    public void onStep() {
    }

}
