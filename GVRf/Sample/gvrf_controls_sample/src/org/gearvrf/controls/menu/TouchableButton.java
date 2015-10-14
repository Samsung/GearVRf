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

package org.gearvrf.controls.menu;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRTexture;
import org.gearvrf.controls.focus.ControlSceneObject;
import org.gearvrf.controls.util.RenderingOrder;

import java.util.ArrayList;

public class TouchableButton extends ControlSceneObject {

    private final int IDLE = 0;
    private final int HOVER = 1;
    private final int PRESSED = 2;

    private ArrayList<GVRTexture> textures;

    public TouchableButton(GVRContext gvrContext, ArrayList<GVRTexture> textures) {
        super(gvrContext);

        this.textures = textures;
        GVRMesh sMesh = getGVRContext().createQuad(0.4f, 0.4f);

        attachRenderData(new GVRRenderData(gvrContext));
        getRenderData().setMaterial(new GVRMaterial(gvrContext));
        getRenderData().setMesh(sMesh);

        getRenderData().getMaterial().setMainTexture(textures.get(IDLE));

        getRenderData().setRenderingOrder(RenderingOrder.MENU_FRAME_TEXT);

        attachEyePointeeHolder();
    }

    @Override
    protected void gainedFocus() {
        getRenderData().getMaterial().setMainTexture(textures.get(HOVER));
    }

    @Override
    protected void lostFocus() {
        getRenderData().getMaterial().setMainTexture(textures.get(IDLE));
    }

    public void pressButton() {
        getRenderData().getMaterial().setMainTexture(textures.get(PRESSED));
    }

    public void unPressButton() {
        getRenderData().getMaterial().setMainTexture(textures.get(HOVER));
    }

}
