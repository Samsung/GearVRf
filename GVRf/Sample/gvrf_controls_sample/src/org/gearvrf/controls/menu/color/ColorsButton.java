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
package org.gearvrf.controls.menu.color;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.controls.R;
import org.gearvrf.controls.menu.MenuControlSceneObject;
import org.gearvrf.controls.shaders.ColorSwapShader;
import org.gearvrf.controls.util.ColorControls.Color;
import org.gearvrf.controls.util.RenderingOrder;

public class ColorsButton extends MenuControlSceneObject {

    private boolean select = false;
    private final float BUTTON_SIZE = 0.22f;
    private final float SELECTED_SIZE = 1.18f;
    private Color color;

    private GVRSceneObject checkObject, hoverObject;

    public ColorsButton(GVRContext gvrContext, Color color) {
        super(gvrContext);

        this.color = color;

        GVRMesh sMesh = getGVRContext().createQuad(BUTTON_SIZE, BUTTON_SIZE);

        attachRenderData(new GVRRenderData(gvrContext));
        getRenderData().setMaterial(
                new GVRMaterial(gvrContext, new ColorSwapShader(gvrContext).getShaderId()));
        getRenderData().setMesh(sMesh);

        setTextures(gvrContext);

        attachEyePointeeHolder();

        createCheckObject();
        createHoverObject();
    }

    private void createCheckObject() {

        GVRMesh checkMesh = getGVRContext().createQuad(BUTTON_SIZE, BUTTON_SIZE);
        GVRTexture checkTexture = getGVRContext().loadTexture(
                new GVRAndroidResource(getGVRContext(), R.drawable.ic_selected_color));

        checkObject = new GVRSceneObject(getGVRContext(), checkMesh, checkTexture);
        checkObject.getRenderData().getMaterial().setOpacity(0);
        checkObject.getTransform().setPositionZ(0.01f);
        checkObject.getRenderData().setRenderingOrder(RenderingOrder.MENU_BUTTON_COLOR - 1);
        
        addChildObject(checkObject);
    }

    private void createHoverObject() {

        GVRMesh checkMesh = getGVRContext().createQuad(BUTTON_SIZE, BUTTON_SIZE);
        GVRTexture checkTexture = getGVRContext().loadTexture(
                new GVRAndroidResource(getGVRContext(), R.drawable.ic_hover_color));

        hoverObject = new GVRSceneObject(getGVRContext(), checkMesh, checkTexture);
        hoverObject.getTransform().setScale(SELECTED_SIZE + 0.1f, SELECTED_SIZE + 0.1f, SELECTED_SIZE + 0.1f);
        hoverObject.getRenderData().getMaterial().setOpacity(0);
        hoverObject.getTransform().setPositionZ(0.011f);
        hoverObject.getRenderData().setRenderingOrder(RenderingOrder.MENU_BUTTON_COLOR);
        
        addChildObject(hoverObject);
    }

    private void setTextures(GVRContext gvrContext) {

        GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                R.drawable.grayscale_circle));

        getRenderData().getMaterial().setTexture(ColorSwapShader.TEXTURE_GRAYSCALE, texture);

        texture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                R.raw.empty));

        getRenderData().getMaterial().setTexture(ColorSwapShader.TEXTURE_DETAILS, texture);
        getRenderData().getMaterial().setVec4(ColorSwapShader.COLOR, this.color.getRed(),
                this.color.getGreen(), this.color.getBlue(), 1);

        getRenderData().setRenderingOrder(RenderingOrder.MENU_GRID_BUTTON);
    }

    private void hover() {
        getTransform().setScale(SELECTED_SIZE, SELECTED_SIZE, SELECTED_SIZE);
        hoverObject.getRenderData().getMaterial().setOpacity(1);
    }

    private void unhover() {
        getTransform().setScale(1, 1, 1);
        hoverObject.getRenderData().getMaterial().setOpacity(0);
    }

    private void checked() {
        checkObject.getRenderData().getMaterial().setOpacity(1);
        hoverObject.getRenderData().getMaterial().setOpacity(0);
    }

    private void unchecked() {
        checkObject.getRenderData().getMaterial().setOpacity(0);
    }
 
    @Override
    protected void gainedFocus() {
        if (!select) {
            hover();
        }
    }

    @Override
    protected void lostFocus() {
        if (!select) {
            unhover();
        }
    }

    @Override
    protected void singleTap() {
        super.singleTap();
        
        if (!select) {
            checked();
            select = true;
        }
    }

    public void unselect() {
        
        unhover();
        unchecked();
        
        select = false;
    }

    public void select() {
        hover();
        checked();
    }

    public Color getColor() {
        return color;
    }
}