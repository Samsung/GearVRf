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

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint.Align;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.controls.R;
import org.gearvrf.controls.focus.ControlSceneObject;
import org.gearvrf.controls.shaders.ButtonShader;
import org.gearvrf.controls.util.GVRTextBitmapFactory;
import org.gearvrf.controls.util.RenderingOrder;
import org.gearvrf.controls.util.Text;

class MenuHeaderItem extends ControlSceneObject {

    private final int IDLE_STATE = 0;
    private final int HOVER_STATE = 1;
    private final int SELECTED_STATE = 2;
    
    private static final int TEXT_TEXTURE_WIDTH = 100;
    private static final int TEXT_TEXTURE_HEIGHT = 40;
    private static final String TEXT_FONT_TYPE = "fonts/samsung-f-bik.ttf";

    private static final float WIDTH = 0.85f;
    private static final float HEIGHT = 0.26f;
    
    private boolean isSelected = false;
    private headerType type;
    
    private ItemSelectedListener onTapListener;
    
    public enum headerType {
        MOTION, COLOR, SCALE, ROTATION
    }

    public MenuHeaderItem(GVRContext gvrContext, String title, headerType type, ItemSelectedListener onTapListener) {
        super(gvrContext);
        
        this.onTapListener = onTapListener;
        this.type = type;
        
        GVRMesh sMesh = getGVRContext().createQuad(WIDTH, HEIGHT);

        attachRenderData(new GVRRenderData(gvrContext));
        getRenderData().setMaterial(new GVRMaterial(gvrContext, new ButtonShader(gvrContext).getShaderId()));
        getRenderData().setMesh(sMesh);
        
        createTextures(gvrContext, title);

        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
        
        attachEyePointeeHolder();
    }
    
    private void createTextures(GVRContext gvrContext, String title) {
        
        Text text = new Text(title, Align.CENTER, 4, Color.parseColor("#4b4b4b"), Color.parseColor("#00ffffff"), 255);
        
        GVRBitmapTexture bitmapIddle = new GVRBitmapTexture(getGVRContext(), createText(text, false));

        getRenderData().getMaterial().setTexture(ButtonShader.STATE1_BACKGROUND_TEXTURE,
                gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.raw.empty)));
        getRenderData().getMaterial().setTexture(ButtonShader.STATE1_TEXT_TEXTURE, bitmapIddle);

        text.textColor = 0xffff6f54;
        
        GVRBitmapTexture bitmapHover = new GVRBitmapTexture(getGVRContext(), createText(text, false));

        getRenderData().getMaterial().setTexture(ButtonShader.STATE2_BACKGROUND_TEXTURE,
                gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.raw.empty)));
        getRenderData().getMaterial().setTexture(ButtonShader.STATE2_TEXT_TEXTURE, bitmapHover);

        text.textColor = 0xffff6f54;

        GVRBitmapTexture bitmapSelected = new GVRBitmapTexture(getGVRContext(), createText(text, true));

        getRenderData().getMaterial().setTexture(ButtonShader.STATE3_BACKGROUND_TEXTURE,
                gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.raw.empty)));
        getRenderData().getMaterial().setTexture(ButtonShader.STATE3_TEXT_TEXTURE, bitmapSelected);
        
        getRenderData().setRenderingOrder(RenderingOrder.MENU_HEADER_TEXT);
    }
    
    public Bitmap createText(Text text, boolean showBottomLine){
        
        if(showBottomLine){
            return GVRTextBitmapFactory.create(getGVRContext().getContext(), TEXT_TEXTURE_WIDTH, TEXT_TEXTURE_HEIGHT, text, TEXT_FONT_TYPE,0);
        } else {
            return GVRTextBitmapFactory.create(getGVRContext().getContext(), TEXT_TEXTURE_WIDTH, TEXT_TEXTURE_HEIGHT, text, TEXT_FONT_TYPE);
        }
    }
    
    @Override
    public void gainedFocus() {
        
        if(!isSelected){
            getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, HOVER_STATE);
        }
    }

    @Override
    public void lostFocus() {
        
        if(!isSelected){
            getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
        }
    }

    @Override
    protected void singleTap() {
        super.singleTap();

        if(!isSelected){
            getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, SELECTED_STATE);
            setSelected(true);
            this.onTapListener.selected(this);
        }
    }

    public void select(){
        
        if(!isSelected){
            getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, SELECTED_STATE);
            setSelected(true);
        }
    }
    
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
    
    public void unselect() {
        isSelected = false;

        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
    }
    
    public headerType getHeaderType() {
        return type;
    }
}