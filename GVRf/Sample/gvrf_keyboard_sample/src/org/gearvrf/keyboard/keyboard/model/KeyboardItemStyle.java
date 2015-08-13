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

import android.content.Context;
import android.content.res.TypedArray;

public class KeyboardItemStyle {

    private float opacityTarget;
    private int fontSize;
    private float characterBackgroundWidth;
    private float characterBackgroundHeight;
    private float sizeQuadHeight;
    private float sizeQuadWidth;
    private float space;
    private int colorText;
    private int hoverTextColor;
    private int colorBackgroundTextHover;
    private int texture;
    private int textureHover;
    private int textureImage;
    private int textureImageHover;
    private int textureType;

    public KeyboardItemStyle(Context context, TypedArray keyboardItemStyle ) {

        opacityTarget = keyboardItemStyle.getFloat(0, 0);
        fontSize = keyboardItemStyle.getInt(1, 0);
        characterBackgroundHeight = keyboardItemStyle.getFloat(2, 0);
        characterBackgroundWidth = keyboardItemStyle.getFloat(3, 0);
        sizeQuadHeight = keyboardItemStyle.getFloat(4, 0);
        sizeQuadWidth = keyboardItemStyle.getFloat(5, 0);
        space = keyboardItemStyle.getFloat(6, 0);
        colorText = keyboardItemStyle.getInt(7, 0);
        hoverTextColor = keyboardItemStyle.getInt(8, 0);
        colorBackgroundTextHover = keyboardItemStyle.getInt(9, 0);
        texture = keyboardItemStyle.getResourceId(10, 0);
        textureHover = keyboardItemStyle.getResourceId(11, 0);
        textureImage =  keyboardItemStyle.getResourceId(12, 0);
        textureImageHover = keyboardItemStyle.getResourceId(13, 0);
        setTextureType(keyboardItemStyle.getInt(14, 0));
        
        keyboardItemStyle.recycle();
    }

    public float getOpacityTarget() {
        return opacityTarget;
    }

    public void setOpacityTarget(float opacityTarget) {
        this.opacityTarget = opacityTarget;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public float getCharacterBackgroundWidth() {
        return characterBackgroundWidth;
    }

    public void setCharacterBackgroundWidth(float characterBackgroundWidth) {
        this.characterBackgroundWidth = characterBackgroundWidth;
    }

    public float getCharacterBackgroundHeight() {
        return characterBackgroundHeight;
    }

    public void setCharacterBackgroundHeight(float characterBackgroundHeight) {
        this.characterBackgroundHeight = characterBackgroundHeight;
    }

    public float getSizeQuadHeight() {
        return sizeQuadHeight;
    }

    public void setSizeQuadHeight(float sizeQuadHeight) {
        this.sizeQuadHeight = sizeQuadHeight;
    }

    public float getSizeQuadWidth() {
        return sizeQuadWidth;
    }

    public void setSizeQuadWidth(float sizeQuadWidth) {
        this.sizeQuadWidth = sizeQuadWidth;
    }

    public float getSpace() {
        return space;
    }

    public void setSpace(float space) {
        this.space = space;
    }

    public int getColorText() {
        return colorText;
    }

    public void setColorText(int colorText) {
        this.colorText = colorText;
    }

    public int getHoverTextColor() {
        return hoverTextColor;
    }

    public void setHoverTextColor(int hoverTextColor) {
        this.hoverTextColor = hoverTextColor;
    }

    public int getColorBackgroundTextHover() {
        return colorBackgroundTextHover;
    }

    public void setColorBackgroundTextHover(int colorBackgroundTextHover) {
        this.colorBackgroundTextHover = colorBackgroundTextHover;
    }

    public int getTexture() {
        return texture;
    }

    public void setTexture(int texture) {
        this.texture = texture;
    }

    public int getTextureHover() {
        return textureHover;
    }

    public void setTextureHover(int textureHover) {
        this.textureHover = textureHover;
    }

    public int getTextureImage() {
        return textureImage;
    }

    public void setTextureImage(int textureImage) {
        this.textureImage = textureImage;
    }

    public int getTextureImageHover() {
        return textureImageHover;
    }

    public void setTextureImageHover(int textureImageHover) {
        this.textureImageHover = textureImageHover;
    }

    public int getTextureType() {
        return textureType;
    }

    public void setTextureType(int textureType) {
        this.textureType = textureType;
    }
}