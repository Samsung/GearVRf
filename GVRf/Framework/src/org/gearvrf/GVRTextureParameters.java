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

    private TextureFilterType minFilterType;
    private TextureFilterType magFilterType;
    private TextureWrapType wrapSType;
    private TextureWrapType wrapTType;

    private float anisotropicValue;

    GVRContext mGVRContext = null;

    public GVRTextureParameters(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        minFilterType = TextureFilterType.GL_LINEAR;
        magFilterType = TextureFilterType.GL_LINEAR;
        wrapSType = TextureWrapType.GL_CLAMP_TO_EDGE;
        wrapTType = TextureWrapType.GL_CLAMP_TO_EDGE;
        anisotropicValue = 1.0f;
    }

    public void setMinFilterType(TextureFilterType minFilterType) {
        this.minFilterType = minFilterType;
    }

    public TextureFilterType getMinFilterType() {
        return minFilterType;
    }

    public void setMagFilterType(TextureFilterType magFilterType) {
        this.magFilterType = magFilterType;
    }

    public TextureFilterType getMagFilterType() {
        return magFilterType;
    }

    public void setWrapSType(TextureWrapType wrapSType) {
        this.wrapSType = wrapSType;
    }

    public TextureWrapType getWrapSType() {
        return wrapSType;
    }

    public void setWrapTType(TextureWrapType wrapTType) {
        this.wrapTType = wrapTType;
    }

    public TextureWrapType getWrapTType() {
        return wrapTType;
    }

    public boolean isAnisotropicSupported() {
        return mGVRContext.isAnisotropicSupported;
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
        if (mGVRContext.isAnisotropicSupported) {
            return mGVRContext.maxAnisotropicValue;
        }
        return -1.0f;
    }

    public float[] getDefalutValuesArray() {
        float[] defaultValues = new float[5];

        defaultValues[0] = GLES20.GL_LINEAR; // MIN FILTER
        defaultValues[1] = GLES20.GL_LINEAR; // MAG FILTER
        defaultValues[2] = 1.0f; // ANISO FILTER
        defaultValues[3] = GLES20.GL_CLAMP_TO_EDGE; // WRAP S
        defaultValues[4] = GLES20.GL_CLAMP_TO_EDGE; // WRAP T

        return defaultValues;
    }

    public float[] getCurrentValuesArray() {
        float[] currentValues = new float[5];

        currentValues[0] = getMinFilterType().getFilterValue(); // MIN FILTER
        currentValues[1] = getMagFilterType().getFilterValue(); // MAG FILTER
        currentValues[2] = getAnisotropicValue(); // ANISO FILTER
        currentValues[3] = getWrapSType().getWrapValue(); // WRAP S
        currentValues[4] = getWrapTType().getWrapValue(); // WRAP T

        return currentValues;
    }

    public enum TextureFilterType {
        GL_LINEAR(GLES20.GL_LINEAR), GL_NEAREST(GLES20.GL_NEAREST), GL_NEAREST_MIPMAP_NEAREST(
                GLES20.GL_NEAREST_MIPMAP_NEAREST), GL_NEAREST_MIPMAP_LINEAR(
                GLES20.GL_NEAREST_MIPMAP_LINEAR), GL_LINEAR_MIPMAP_NEAREST(
                GLES20.GL_LINEAR_MIPMAP_NEAREST), GL_LINEAR_MIPMAP_LINEAR(
                GLES20.GL_LINEAR_MIPMAP_LINEAR);

        private int filterValue;

        TextureFilterType(int filterValue) {
            this.filterValue = filterValue;
        }

        public int getFilterValue() {
            return filterValue;
        }
    }

    public enum TextureWrapType {
        GL_CLAMP_TO_EDGE(GLES20.GL_CLAMP_TO_EDGE), GL_MIRRORED_REPEAT(
                GLES20.GL_MIRRORED_REPEAT), GL_REPEAT(GLES20.GL_REPEAT);

        private int wrapValue;

        TextureWrapType(int wrapValue) {
            this.wrapValue = wrapValue;
        }

        public int getWrapValue() {
            return wrapValue;
        }
    }
}

class NativeTextureParameters {
    static native float getMaxAnisotropicValue();
}
