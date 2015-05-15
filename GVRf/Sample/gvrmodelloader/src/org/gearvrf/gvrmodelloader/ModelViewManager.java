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

package org.gearvrf.gvrmodelloader;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;

public class ModelViewManager extends GVRScript {

    @Override
    public void onInit(GVRContext gvrContext) {
        GVRSceneObject modelAsScenObject = gvrContext.loadModel("sponza.dae"); //Use walled_cube.dae as file name to test the simple walled cube model

        //Un-comment the following line to test the model as a complete scene
        //gvrContext.loadScene("sponza.dae");   // Loads the model as a complete scene

        gvrContext.getMainScene().addSceneObject(modelAsScenObject);
        gvrContext.getMainScene().getMainCameraRig().getOwnerObject().getTransform().setPosition(5.0f, 5.0f, 0.0f);
    }

    @Override
    public void onStep() {
    }
}
