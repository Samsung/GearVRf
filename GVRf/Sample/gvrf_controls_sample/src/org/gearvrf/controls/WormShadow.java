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

package org.gearvrf.controls;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderPass.GVRCullFaceEnum;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;


public class WormShadow extends GVRSceneObject {

    public WormShadow(GVRContext gvrContext, float width, float height, int renderingOrder) {
        super(gvrContext);

        createShadowObject(width, height, renderingOrder);
    }

    private void createShadowObject(float width, float height, int renderingOrder) {

        GVRMesh checkMesh = getGVRContext().createQuad(width, height);

        GVRTexture checkTexture = getGVRContext().loadTexture(
                new GVRAndroidResource(getGVRContext(), R.drawable.shadow));

        GVRSceneObject shadowObject = new GVRSceneObject(getGVRContext(), checkMesh, checkTexture);
        shadowObject.getTransform().rotateByAxis(90, 1, 0, 0);
        shadowObject.getRenderData().setRenderingOrder(renderingOrder);

        shadowObject.getRenderData().setCullFace(GVRCullFaceEnum.None);

        addChildObject(shadowObject);
    }
}
