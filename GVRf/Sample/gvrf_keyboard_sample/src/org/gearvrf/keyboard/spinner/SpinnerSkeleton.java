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

package org.gearvrf.keyboard.spinner;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.keyboard.R;
import org.gearvrf.keyboard.util.RenderingOrder;
import org.gearvrf.keyboard.util.SceneObjectNames;

public class SpinnerSkeleton extends GVRSceneObject {

    private GVRContext gvrContext;
    private GVRSceneObject spinnerBox;
    private GVRSceneObject spinnerShadow;

    public SpinnerSkeleton(GVRContext gvrContext) {
        super(gvrContext);

        setName(SceneObjectNames.SPINNER_SKELETON);
        this.gvrContext = gvrContext;
        createSkeletonSpinner();
    }

    private GVRSceneObject createSkeletonSpinner() {

        spinnerBox = getSpinnerBackground(R.drawable.spinner_asset_box);
        spinnerShadow = getSpinnerBackground(R.drawable.spinner_asset_shadow);

        spinnerBox.getRenderData().setRenderingOrder(RenderingOrder.SPINNER_BOX);
        spinnerShadow.getRenderData().setRenderingOrder(RenderingOrder.SPINNER_SHADOW);

        spinnerBox.attachEyePointeeHolder();
        addChildObject(spinnerBox);
        addChildObject(spinnerShadow);

        return this;

    }

    private GVRSceneObject getSpinnerBackground(int resourceTextureID) {

        GVRSceneObject object = new GVRSceneObject(gvrContext);
        GVRRenderData renderData = new GVRRenderData(gvrContext);
        GVRMaterial material = new GVRMaterial(gvrContext);
        GVRMesh mesh = gvrContext.createQuad(0.49f / 2, 1.63f / 2 /**
         * - 0.01f
         * 1.1f
         */
        );

        renderData.setMaterial(material);
        renderData.setMesh(mesh);
        object.attachRenderData(renderData);
        object.getRenderData()
                .getMaterial()
                .setMainTexture(
                        gvrContext.loadTexture(new GVRAndroidResource(gvrContext.getActivity(),
                                resourceTextureID)));
        return object;

    }

    public GVRSceneObject getSpinnerBox() {
        return spinnerBox;
    }

    public GVRSceneObject getSpinnerShadow() {
        return spinnerShadow;
    }

}
