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

package org.gearvrf.samples.Minimal360Photo;

import java.io.IOException;
import java.util.concurrent.Future;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRSphereSceneObject;

import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

public class Minimal360PhotoScript extends GVRScript {

    private final String TAG = "Minimal360PhotoScript";
    private Bitmap mIncomingBitmap = null;

    @Override
    public void onInit(GVRContext gvrContext) {

        android.util.Log.d(TAG, "in onInit");
        // get a handle to the scene
        GVRScene scene = gvrContext.getNextMainScene();

        GVRSphereSceneObject sphereObject = null;

        if(mIncomingBitmap == null) {
            // load texture
            Future<GVRTexture> texture = gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.raw.photosphere));

            // create a sphere scene object with the specified texture and triangles facing inward (the 'false' argument) 
            sphereObject = new GVRSphereSceneObject(gvrContext, false, texture);
        } else {
            // create a sphere scene object with the triangles facing inward (the 'false' argument) 
            sphereObject = new GVRSphereSceneObject(gvrContext, false);
            // set the texture to be the specified incoming texture
            GVRBitmapTexture incomingBitmapTexture = new GVRBitmapTexture(gvrContext, mIncomingBitmap);
            sphereObject.getRenderData().getMaterial().setMainTexture(incomingBitmapTexture);
        }

        // add the scene object to the scene graph
        scene.addSceneObject(sphereObject);
    }

    public void setPhotosphere(GVRActivity activity, Uri uri) {
        try {
            mIncomingBitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
        } catch (IOException e) {
            android.util.Log.e(TAG, "Retrieving the bitmap failed.");
        }
    }

    @Override
    public void onStep() {
    }

}
