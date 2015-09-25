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

package org.gearvrf.controls.menu.motion;

import android.graphics.Color;
import android.graphics.Paint.Align;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.controls.R;
import org.gearvrf.controls.menu.MenuControlSceneObject;
import org.gearvrf.controls.model.Apple.Motion;
import org.gearvrf.controls.shaders.ButtonShader;
import org.gearvrf.controls.util.GVRTextBitmapFactory;
import org.gearvrf.controls.util.RenderingOrder;
import org.gearvrf.controls.util.Text;

public class MotionButton extends MenuControlSceneObject {

    private final int IDLE_STATE = 0;
    private final int HOVER_STATE = 1;
    private final int SELECTED_STATE = 2;

    private boolean select = false;
    private final int WIDTH = 90;
    private final int HEIGHT = 20;
  
    private String title;
    private Motion motion;
    
    public MotionButton(GVRContext gvrContext, String title, Motion motion) {
        super(gvrContext);
        
        this.motion = motion;
        this.title = title;
   
        GVRMesh sMesh = getGVRContext().createQuad(0.9f, 0.2f);

        attachRenderData(new GVRRenderData(gvrContext));
        getRenderData().setMaterial(new GVRMaterial(gvrContext, new ButtonShader(gvrContext).getShaderId()));
        getRenderData().setMesh(sMesh);
        
        createTextures();

        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
        getRenderData().setRenderingOrder(RenderingOrder.MENU_FRAME_TEXT);
        
        attachEyePointeeHolder();
    }
    

    public Motion getMotion() {
        return motion;
    }

    private void createTextures() {
       
        Text text = new Text(title, Align.LEFT, 3.5f, Color.parseColor("#ffffff"), Color.parseColor("#00000000"), 45);
        String font = "fonts/samsung-if-bold.ttf";
        
        GVRBitmapTexture bitmapIddle = new GVRBitmapTexture(getGVRContext(),
                GVRTextBitmapFactory.create(getGVRContext().getContext(), WIDTH, HEIGHT, text, font));

        getRenderData().getMaterial().setTexture(ButtonShader.STATE1_BACKGROUND_TEXTURE,
                getGVRContext().loadTexture(new GVRAndroidResource(getGVRContext(), R.raw.empty)));
        getRenderData().getMaterial().setTexture(ButtonShader.STATE1_TEXT_TEXTURE, bitmapIddle);

        text.textSize = 4;

        GVRBitmapTexture bitmapHover = new GVRBitmapTexture(getGVRContext(),
                GVRTextBitmapFactory.create(getGVRContext().getContext(), WIDTH, HEIGHT, text, font));

        getRenderData().getMaterial().setTexture(ButtonShader.STATE2_BACKGROUND_TEXTURE,
                getGVRContext().loadTexture(new GVRAndroidResource(getGVRContext(), R.raw.empty)));
        getRenderData().getMaterial().setTexture(ButtonShader.STATE2_TEXT_TEXTURE, bitmapHover);

        text.textColor = 0xfff8DF35;

        GVRBitmapTexture bitmapSelected = new GVRBitmapTexture(getGVRContext(),
                GVRTextBitmapFactory.create(getGVRContext().getContext(), WIDTH, HEIGHT, text, font));

        getRenderData().getMaterial().setTexture(ButtonShader.STATE3_BACKGROUND_TEXTURE,
                getGVRContext().loadTexture(new GVRAndroidResource(getGVRContext(), R.raw.empty)));
        getRenderData().getMaterial().setTexture(ButtonShader.STATE3_TEXT_TEXTURE, bitmapSelected);
    }

    @Override
    protected void gainedFocus() {
        if (!select) {
            getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, HOVER_STATE);
        }
    }

    @Override
    protected void lostFocus() {
        if (!select) {
            getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
        }
    }

    @Override
    protected void singleTap() {
        super.singleTap();
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, SELECTED_STATE);
        select = true;
    }

    public void unselect() {
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, IDLE_STATE);
        select = false;
    }
    
    public void select(){
        getRenderData().getMaterial().setFloat(ButtonShader.TEXTURE_SWITCH, SELECTED_STATE);
        select = true;
    }
}