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

package org.gearvrf;

import android.opengl.GLES20;

public class GVRTextureParameters {

    private TextureMinFilterType minFilterType;
    private int minFilterTypeValue;

    private TextureMagFilterType magFilterType;
    private int magFilterTypeValue;
    
    private TextureWrapSType wrapSType;
    private int wrapSTypeValue;

    private TextureWrapTType wrapTType;
    private int wrapTTypeValue;
    
    private String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
    private boolean anisotropicSupported;
    private float anisotropicValue;

    public GVRTextureParameters() {
        minFilterType = TextureMinFilterType.GL_LINEAR;
        minFilterTypeValue = 0;

        magFilterType = TextureMagFilterType.GL_LINEAR;
        magFilterTypeValue = 0;
        
        wrapSType = TextureWrapSType.GL_CLAMP_TO_EDGE;
        wrapSTypeValue = 0;
        
        wrapTType = TextureWrapTType.GL_CLAMP_TO_EDGE;
        wrapTTypeValue = 0;

        anisotropicSupported = false;
        anisotropicValue = 1.0f;
    }

    public void setMinFilterType(TextureMinFilterType minFilterType) {
        this.minFilterType = minFilterType;
    }

    public TextureMinFilterType getMinFilterType() {
        return minFilterType;
    }

    public int getMinFilterTypeValue() {
        switch (minFilterType) {
        case GL_LINEAR:
            minFilterTypeValue = 0;
            break;
        case GL_NEAREST:
            minFilterTypeValue = 1;
            break;
        case GL_NEAREST_MIPMAP_NEAREST:
            minFilterTypeValue = 2;
            break;
        case GL_LINEAR_MIPMAP_NEAREST:
            minFilterTypeValue = 3;
            break;
        case GL_NEAREST_MIPMAP_LINEAR:
            minFilterTypeValue = 4;
            break;
        case GL_LINEAR_MIPMAP_LINEAR:
            minFilterTypeValue = 5;
            break;
        default:
            minFilterTypeValue = 0;
            break;
        }
        return minFilterTypeValue;
    }

    public void setMagFilterType(TextureMagFilterType magFilterType) {
        this.magFilterType = magFilterType;
    }

    public TextureMagFilterType getMagFilterType() {
        return magFilterType;
    }

    public int getMagFilterTypeValue() {
        switch (magFilterType) {
        case GL_LINEAR:
            magFilterTypeValue = 0;
            break;
        case GL_NEAREST:
            magFilterTypeValue = 1;
            break;
        default:
            magFilterTypeValue = 0;
            break;
        }
        return magFilterTypeValue;
    }

    public void setWrapSType(TextureWrapSType wrapSType) {
        this.wrapSType = wrapSType;
    }

    public TextureWrapSType getWrapSType() {
        return wrapSType;
    }

    public int getWrapSTypeValue() {
        switch (wrapSType) {
        case GL_CLAMP_TO_EDGE:
            wrapSTypeValue = 0;
            break;
        case GL_MIRRORED_REPEAT:
            wrapSTypeValue = 1;
            break;
        case GL_REPEAT:
            wrapSTypeValue = 2;
            break;
        default:
            wrapSTypeValue = 0;
            break;
        }
        return wrapSTypeValue;
    }
    
    public void setWrapTType(TextureWrapTType wrapTType) {
        this.wrapTType = wrapTType;
    }

    public TextureWrapTType getWrapTType() {
        return wrapTType;
    }

    public int getWrapTTypeValue() {
        switch (wrapTType) {
        case GL_CLAMP_TO_EDGE:
            wrapTTypeValue = 0;
            break;
        case GL_MIRRORED_REPEAT:
            wrapTTypeValue = 1;
            break;
        case GL_REPEAT:
            wrapTTypeValue = 2;
            break;
        default:
            wrapTTypeValue = 0;
            break;
        }
        return wrapTTypeValue;
    }
    
    public boolean isAnisotropicSupported() {
        anisotropicSupported = extensions
                .contains("GL_EXT_texture_filter_anisotropic");
        return anisotropicSupported;
    }

    public void setAnisotropicValue(float value) {
        if (value > getMaxAnisotropicValue()) {
            value = getMaxAnisotropicValue();
        }
        anisotropicValue = value;
    }

    public float getAnisotropicValue() {
        return anisotropicValue;
    }

    public float getMaxAnisotropicValue() {
        return NativeTextureParameters.getMaxAnisotropicValue();
    }

    public float[] getDefalutValuesArray() {
        float[] defaultValues = new float[5];

        defaultValues[0] = 0; // MIN FILTER
        defaultValues[1] = 0; // MAG FILTER
        defaultValues[2] = 1.0f; // ANISO FILTER
        defaultValues[3] = 0; // WRAP S
        defaultValues[4] = 0; // WRAP T

        return defaultValues;
    }

    public float[] getCurrentValuesArray() {
        float[] currentValues = new float[5];

        currentValues[0] = getMinFilterTypeValue(); // MIN FILTER
        currentValues[1] = getMagFilterTypeValue(); // MAG FILTER
        currentValues[2] = getAnisotropicValue(); // ANISO FILTER
        currentValues[3] = getWrapSTypeValue(); // WRAP S
        currentValues[4] = getWrapTTypeValue(); // WRAP T

        return currentValues;
    }

    public enum TextureMinFilterType {
        GL_LINEAR, GL_NEAREST, GL_NEAREST_MIPMAP_NEAREST, GL_LINEAR_MIPMAP_NEAREST, GL_NEAREST_MIPMAP_LINEAR, GL_LINEAR_MIPMAP_LINEAR
    }

    public enum TextureMagFilterType {
        GL_LINEAR, GL_NEAREST
    }
    
    public enum TextureWrapSType {
        GL_CLAMP_TO_EDGE, GL_MIRRORED_REPEAT, GL_REPEAT
    }
    
    public enum TextureWrapTType {
        GL_CLAMP_TO_EDGE, GL_MIRRORED_REPEAT, GL_REPEAT
    }
}

class NativeTextureParameters {
    static native float getMaxAnisotropicValue();
}
