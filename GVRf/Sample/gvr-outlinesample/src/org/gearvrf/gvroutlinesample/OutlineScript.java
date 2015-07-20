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

package org.gearvrf.gvroutlinesample;

import java.io.IOException;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRRenderPass;
import org.gearvrf.GVRRenderPass.GVRCullFaceEnum;
import org.gearvrf.GVRImportSettings;

import android.util.Log;

public class OutlineScript extends GVRScript {

    private GVRContext mGVRContext;
    private GVRSceneObject mCharacter;

    private static final float ROTATION_SPEED = 0.75f;
    private final String mModelPath = "FreeCharacter_01.fbx";
    private final String mDiffuseTexturePath = "Body_Diffuse_01.jpg";
    
    private GVRActivity mActivity;
     
    private static final String TAG = OutlineScript.class.getName();
 
    public OutlineScript(GVRActivity activity) {
      mActivity = activity;
    }
    
    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        GVRScene outlineScene = gvrContext.getNextMainScene();
        
        try {
          GVRImportSettings settings = new GVRImportSettings(GVRImportSettings.CALCULATE_SMOOTH_NORMALS 
                  | GVRImportSettings.CALCULATE_TANGENTS);
          GVRMesh characterMesh = mGVRContext.loadMesh(new GVRAndroidResource(mGVRContext,
              mModelPath), settings);
          
          // Setup Scene - Alternatively to set character transform, one could
          // achieve same effect by setting camera transform (outlineScene->getMainCameraRig)
          // passing inverse transformation values.
          mCharacter = new GVRSceneObject(mGVRContext, characterMesh);
          mCharacter.getTransform().setPosition(0.0f, -300.0f, -200.0f);
          mCharacter.getTransform().setRotationByAxis(-90.0f, 1.0f, 0.0f, 0.0f);

          // Create Base Material Pass
          // ---------------------------------------------------------------
          OutlineShader outlineShader = new OutlineShader(mGVRContext);
          GVRMaterial outlineMaterial = new GVRMaterial(mGVRContext,
                  outlineShader.getShaderId());
  
          // Brown-ish outline color
          outlineMaterial.setVec4(OutlineShader.COLOR_KEY, 0.4f, 0.1725f,
                  0.1725f, 1.0f);
          outlineMaterial.setFloat(OutlineShader.THICKNESS_KEY, 2.0f);
  
          // For outline we want to cull front faces
          mCharacter.getRenderData().setMaterial(outlineMaterial);
          mCharacter.getRenderData().setCullFace(GVRCullFaceEnum.Front);

          // Create Additional Pass
          // ----------------------------------------------------------------
          // load texture
          GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(
                  mGVRContext, mDiffuseTexturePath));

          GVRMaterial material = new GVRMaterial(mGVRContext,
                  GVRShaderType.Texture.ID);
          material.setMainTexture(texture);

          GVRRenderPass pass = new GVRRenderPass(mGVRContext);
          pass.setMaterial(material);
          pass.setCullFace(GVRCullFaceEnum.Back);
          mCharacter.getRenderData().addPass(pass);
          
          // Finally Add Cube to Scene
          outlineScene.addSceneObject(mCharacter);
          
        } catch(IOException e) {
            e.printStackTrace();
            mActivity.finish();
            mActivity = null;
            Log.e(TAG, "One or more assets could not be loaded.");
        }
    }

    @Override
    public void onStep() {
        if (mCharacter != null) {
          mCharacter.getTransform().rotateByAxis(ROTATION_SPEED, 0.0f, 1.0f, 0.0f);
        }
    }

}
