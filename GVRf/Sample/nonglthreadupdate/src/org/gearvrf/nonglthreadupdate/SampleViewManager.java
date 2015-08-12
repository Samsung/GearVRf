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

package org.gearvrf.nonglthreadupdate;

import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.debug.GVRConsole;
import org.gearvrf.debug.GVRConsole.EyeMode;
import org.gearvrf.nontlthreadupdate.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

public class SampleViewManager extends GVRScript {

    private GVRConsole mConsole;
    GVRBitmapTexture mTexture;
    
    private Bitmap[] bitmap = new Bitmap[3];
    
    @Override
    public void onInit(GVRContext gvrContext) {

        // save context for possible use in onStep(), even though that's empty
        // in this sample

        GVRScene scene = gvrContext.getNextMainScene();
        mConsole = new GVRConsole(gvrContext, EyeMode.BOTH_EYES, gvrContext.getNextMainScene());
        mConsole.setTextSize(mConsole.getTextSize() * 3);
        // set background color
        GVRCameraRig mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.getLeftCamera()
                .setBackgroundColor(Color.GRAY);
        mainCameraRig.getRightCamera()
                .setBackgroundColor(Color.GRAY);
        
        bitmap[0] = BitmapFactory.decodeResource(gvrContext.getActivity().getResources(), R.drawable.gearvr_logo);
        bitmap[1] = BitmapFactory.decodeResource(gvrContext.getActivity().getResources(), R.drawable.left);
        bitmap[2] = BitmapFactory.decodeResource(gvrContext.getActivity().getResources(), R.drawable.right);
        mTexture = new GVRBitmapTexture(gvrContext, bitmap[0]);

        // create a scene object (this constructor creates a rectangular scene
        // object that uses the standard 'unlit' shader)
        GVRSceneObject sceneObject = new GVRSceneObject(gvrContext, 4.0f, 2.0f,
                mTexture);

        // set the scene object position
        sceneObject.getTransform().setPosition(0.0f, 0.0f, -3.0f);

        // add the scene object to the scene graph
        scene.addSceneObject(sceneObject);

    }
    
    int counter = 0;
    
    @Override
    public void onStep() {
    }
    
    public void onTapUp(){
        counter++;
        mConsole.clear();
        counter %= 3;
        mConsole.writeLine("TextureNumber Number %d", counter);
        mTexture.update(bitmap[counter]);
    }

}
