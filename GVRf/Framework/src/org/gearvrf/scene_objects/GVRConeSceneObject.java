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
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.utility.Log;

public class GVRConeSceneObject extends GVRSceneObject {

    @SuppressWarnings("unused")
    private static final String TAG = Log.tag(GVRConeSceneObject.class);
    
    private static final int STACK_NUMBER = 10;
    private static final int SLICE_NUMBER = 36;
    private static final float BASE_RADIUS = 0.5f;
    private static final float TOP_RADIUS = 0.0f;
    private static final float HEIGHT = 1.0f;

    /**
     * Constructs a cone scene object with a height of 1, and a radius of 0.5.
     * 
     * @param gvrContext
     *            current {@link GVRContext}
     */
    public GVRConeSceneObject(GVRContext gvrContext) {
        super(gvrContext);

        @SuppressWarnings("resource")
        GVRCylinderSceneObject cylinder = new GVRCylinderSceneObject(
                gvrContext, BASE_RADIUS, TOP_RADIUS, HEIGHT, STACK_NUMBER,
                SLICE_NUMBER);

        GVRMesh mesh = cylinder.getRenderData().getMesh();
        GVRRenderData renderData = new GVRRenderData(gvrContext);
        attachRenderData(renderData);
        renderData.setMesh(mesh);
    }

}
