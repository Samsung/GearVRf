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
package org.gearvrf.keyboard.keyboard.model;

import org.gearvrf.GVRContext;
import org.gearvrf.keyboard.keyboard.numeric.Keyboard;
import org.gearvrf.keyboard.model.KeyboardCharItem;
import org.gearvrf.keyboard.shader.TransparentButtonShaderThreeStates;
import org.gearvrf.keyboard.util.SceneObjectNames;

public class KeyboardSoftItem extends KeyboardItemBase {
    
    private static final int RESOURCE = 1, Mix = 2;

    public KeyboardSoftItem(GVRContext gvrContext, KeyboardCharItem mCharItem, KeyboardItemStyle styleItem) {
        super(gvrContext, styleItem);
        setName(SceneObjectNames.KEYBOARD_ITEM);

        this.keyboardCharItem = mCharItem;
        
        TransparentButtonShaderThreeStates dif = new TransparentButtonShaderThreeStates(gvrContext);
        createTextures(dif);

        configureTextures();
    }

    @Override
    public void configureTextures() {
        super.configureTextures();
        
        switch (styleItem.getTextureType()) {

            case RESOURCE:
                
                setTextureFromResource(TransparentButtonShaderThreeStates.TEXTURE_TEXT_KEY, styleItem.getTextureImage());
                setTextureFromResource(TransparentButtonShaderThreeStates.TEXTURE_TEXT_HOVER_KEY, styleItem.getTextureImageHover());
                
                setTextureFromResource(TransparentButtonShaderThreeStates.TEXTURE_TEXT_UPPER_KEY, styleItem.getTextureImage());
                setTextureFromResource(TransparentButtonShaderThreeStates.TEXTURE_TEXT_HOVER_UPPER_KEY, styleItem.getTextureImageHover());
                
                setTextureFromResource(TransparentButtonShaderThreeStates.TEXTURE_TEXT_SPECIAL_KEY, styleItem.getTextureImage());
                setTextureFromResource(TransparentButtonShaderThreeStates.TEXTURE_TEXT_HOVER_SPECIAL_KEY, styleItem.getTextureImageHover());
                
                break;
            case Mix:
                
                setTextureFromResource(TransparentButtonShaderThreeStates.TEXTURE_TEXT_KEY, styleItem.getTextureImage());
                setTextureFromResource(TransparentButtonShaderThreeStates.TEXTURE_TEXT_HOVER_KEY, styleItem.getTextureImageHover());
                
                setTextureFromResource(TransparentButtonShaderThreeStates.TEXTURE_TEXT_UPPER_KEY, styleItem.getTextureImage());
                setTextureFromResource(TransparentButtonShaderThreeStates.TEXTURE_TEXT_HOVER_UPPER_KEY, styleItem.getTextureImageHover());
                
                setNomalTexture(keyboardCharItem.getSpecialCharacter(), TransparentButtonShaderThreeStates.TEXTURE_TEXT_SPECIAL_KEY);
                setHoverTexture(keyboardCharItem.getSpecialCharacter(), TransparentButtonShaderThreeStates.TEXTURE_TEXT_HOVER_SPECIAL_KEY);
                
                break;
            default:
                
                setNomalTexture(keyboardCharItem.getCharacter(), TransparentButtonShaderThreeStates.TEXTURE_TEXT_KEY);
                setHoverTexture(keyboardCharItem.getCharacter(), TransparentButtonShaderThreeStates.TEXTURE_TEXT_HOVER_KEY);
                
                setNomalTexture(keyboardCharItem.getCharacter().toUpperCase(), TransparentButtonShaderThreeStates.TEXTURE_TEXT_UPPER_KEY);
                setHoverTexture(keyboardCharItem.getCharacter().toUpperCase(), TransparentButtonShaderThreeStates.TEXTURE_TEXT_HOVER_UPPER_KEY);
              
                setNomalTexture(keyboardCharItem.getSpecialCharacter(), TransparentButtonShaderThreeStates.TEXTURE_TEXT_SPECIAL_KEY);
                setHoverTexture(keyboardCharItem.getSpecialCharacter(), TransparentButtonShaderThreeStates.TEXTURE_TEXT_HOVER_SPECIAL_KEY);
                
                break;
        }
    }

    @Override
    public void switchMaterialState(int state) {

        switch (state) {
            
            case Keyboard.NUMERIC_KEYBOARD:
                
                getRenderData().getMaterial().setFloat(TransparentButtonShaderThreeStates.TEXTURE_SWITCH, 2.0f); 
                break;

            case Keyboard.SOFT_KEYBOARD_UPPERCASE:
                
                getRenderData().getMaterial().setFloat(TransparentButtonShaderThreeStates.TEXTURE_SWITCH, 2.0f); 
                break;

            case Keyboard.SOFT_KEYBOARD_LOWERCASE:
                
                getRenderData().getMaterial().setFloat(TransparentButtonShaderThreeStates.TEXTURE_SWITCH, 0.0f);
                break;

            case Keyboard.SOFT_KEYBOARD_SPECIAL:
                
                getRenderData().getMaterial().setFloat(TransparentButtonShaderThreeStates.TEXTURE_SWITCH, 4.0f);
                break;

            default:
                break;
        }
    }

    public void setNormalMaterial() {
        
        switch (Keyboard.mode) {
            
            case Keyboard.NUMERIC_KEYBOARD:
                
                getRenderData().getMaterial().setFloat(TransparentButtonShaderThreeStates.TEXTURE_SWITCH, 2.0f); 
                break;

            case Keyboard.SOFT_KEYBOARD_UPPERCASE:
                
                getRenderData().getMaterial().setFloat(TransparentButtonShaderThreeStates.TEXTURE_SWITCH, 2.0f); 
                break;

            case Keyboard.SOFT_KEYBOARD_LOWERCASE:
                
                getRenderData().getMaterial().setFloat(TransparentButtonShaderThreeStates.TEXTURE_SWITCH, 0.0f);
                break;

            case Keyboard.SOFT_KEYBOARD_SPECIAL:
                
                getRenderData().getMaterial().setFloat(TransparentButtonShaderThreeStates.TEXTURE_SWITCH, 4.0f);
                break;

            default:
                
                getRenderData().getMaterial().setFloat(TransparentButtonShaderThreeStates.TEXTURE_SWITCH, 0.0f);
        }
    }

    public void setHoverMaterial() {

        switch (Keyboard.mode) {
            
            case Keyboard.NUMERIC_KEYBOARD:
                
                getRenderData().getMaterial().setFloat(TransparentButtonShaderThreeStates.TEXTURE_SWITCH, 3.0f); 
                break;

            case Keyboard.SOFT_KEYBOARD_UPPERCASE:
                
                getRenderData().getMaterial().setFloat(TransparentButtonShaderThreeStates.TEXTURE_SWITCH, 3.0f);
                break;

            case Keyboard.SOFT_KEYBOARD_LOWERCASE:
                
                getRenderData().getMaterial().setFloat(TransparentButtonShaderThreeStates.TEXTURE_SWITCH, 1.0f);
                break;

            case Keyboard.SOFT_KEYBOARD_SPECIAL:
                
                getRenderData().getMaterial().setFloat(TransparentButtonShaderThreeStates.TEXTURE_SWITCH, 5.0f);
                break;

            default:
                break;
        }
    }
}