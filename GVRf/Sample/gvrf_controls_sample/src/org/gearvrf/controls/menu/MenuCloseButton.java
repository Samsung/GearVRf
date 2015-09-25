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

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.controls.R;
import org.gearvrf.controls.focus.ControlSceneObject;
import org.gearvrf.controls.shaders.ButtonShader;
import org.gearvrf.controls.util.RenderingOrder;

public class MenuCloseButton extends ControlSceneObject {

    private final int IDLE_STATE = 0;
    private final int HOVER_STATE = 1;
    private final int SELECTED_STATE = 2;

    private GVROpacityAnimation opacityShow;
    private GVROpacityAnimation opacityHide;

    public MenuCloseButton(GVRContext gvrContext) {
        super(gvrContext);

        GVRMesh sMesh = getGVRContext().createQuad(0.4f, 0.4f);

        attachRenderData(new GVRRenderData(gvrContext));
        getRenderData().setMaterial(new GVRMaterial(gvrContext, new ButtonShader(gvrContext).getShaderId()));
        getRenderData().setMesh(sMesh);
        createTextures(gvrContext);

        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
        getRenderData().setRenderingOrder(RenderingOrder.MENU_FRAME_TEXT + 1);
        
        attachEyePointeeHolder();
    }

    private void createTextures(GVRContext gvrContext) {
        
        GVRTexture empty = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.raw.empty));
        GVRTexture idle = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.bt_close));
        GVRTexture hover = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.bt_close_hover));
        GVRTexture selected = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.bt_close_pressed));
        
        getRenderData().getMaterial().setTexture(ButtonShader.STATE1_BACKGROUND_TEXTURE, empty);
        getRenderData().getMaterial().setTexture(ButtonShader.STATE1_TEXT_TEXTURE, idle);

        getRenderData().getMaterial().setTexture(ButtonShader.STATE2_BACKGROUND_TEXTURE, empty);
        getRenderData().getMaterial().setTexture(ButtonShader.STATE2_TEXT_TEXTURE, hover);

        getRenderData().getMaterial().setTexture(ButtonShader.STATE3_BACKGROUND_TEXTURE, empty);
        getRenderData().getMaterial().setTexture(ButtonShader.STATE3_TEXT_TEXTURE, selected);
    }

    @Override
    protected void gainedFocus() {
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, HOVER_STATE);
    }

    @Override
    protected void lostFocus() {
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
    }

    @Override
    protected void singleTap() {
        super.singleTap();
        
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, SELECTED_STATE);
        
    }

    public void unselect() {
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
    }
    
    private void stop(){
        
        if(opacityShow != null){
            getGVRContext().getAnimationEngine().stop(opacityShow);
        }
        
        if(opacityHide != null){
            getGVRContext().getAnimationEngine().stop(opacityHide);
        }
    }
    
    public void show(){
    
        stop();
        
        opacityShow = new GVROpacityAnimation(this, 1f, 1);
        opacityShow.setRepeatMode(GVRRepeatMode.ONCE);
        opacityShow.start(getGVRContext().getAnimationEngine());
    }
    
    public void hide(){
        
        stop();
        
        opacityHide = new GVROpacityAnimation(this, 0.3f, 0);
        opacityHide.setRepeatMode(GVRRepeatMode.ONCE);
        opacityHide.start(getGVRContext().getAnimationEngine());
    }
}