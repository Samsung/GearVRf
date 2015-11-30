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

import java.util.concurrent.Future;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRSphereSceneObject;

public class Minimal360PhotoScript extends GVRScript {

    @Override
    public void onInit(GVRContext gvrContext) {

        // get a handle to the scene
        GVRScene scene = gvrContext.getNextMainScene();

        GVRSphereSceneObject sphereObject = null;

        // load texture
        Future<GVRTexture> texture = gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.raw.photosphere));

        // create a sphere scene object with the specified texture and triangles facing inward (the 'false' argument) 
        sphereObject = new GVRSphereSceneObject(gvrContext, false, texture);

        // add the scene object to the scene graph
        scene.addSceneObject(sphereObject);
    }

    @Override
    public void onStep() {
    }

}
