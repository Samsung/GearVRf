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

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

/**
 * The class to be used to set and pass texture filter and warp types for
 * loading texture and also to enhance texture even after they are loaded.
 */
public class GVRTextureParameters {

    private TextureFilterType minFilterType;
    private TextureFilterType magFilterType;
    private TextureWrapType wrapSType;
    private TextureWrapType wrapTType;

    private int anisotropicValue;

    GVRContext mGVRContext = null;

    /**
     * Constructs a texture parameter object with default values for filter and
     * warp.
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     */
    public GVRTextureParameters(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        minFilterType = TextureFilterType.GL_LINEAR;
        magFilterType = TextureFilterType.GL_LINEAR;
        wrapSType = TextureWrapType.GL_CLAMP_TO_EDGE;
        wrapTType = TextureWrapType.GL_CLAMP_TO_EDGE;
        anisotropicValue = 1;
    }

    /**
     * Sets the MIN filter for texture.
     * 
     * @param minFilterType
     *            Basically, a GL constant that represents the type of filter
     *            one want to apply.
     */
    public void setMinFilterType(TextureFilterType minFilterType) {
        this.minFilterType = minFilterType;
    }

    /**
     * Returns the MIN filter of the {@link GVRTextureParameters}.
     * 
     * The returned value could be the one that was set by the calling API or
     * the default value for all the objects.
     * 
     * @return The MIN filter type as a TextureFilterType.
     */
    public TextureFilterType getMinFilterType() {
        return minFilterType;
    }

    /**
     * Sets the MAG filter for texture.
     * 
     * @param magFilterType
     *            Basically, a GL constant that represents the type of filter
     *            one want to apply.
     */
    public void setMagFilterType(TextureFilterType magFilterType) {
        this.magFilterType = magFilterType;
    }

    /**
     * Returns the MAG filter of the {@link GVRTextureParameters}.
     * 
     * The returned value could be the one that was set by the calling API or
     * the default value for all the objects.
     * 
     * @return The MAG filter type as a TextureFilterType.
     */
    public TextureFilterType getMagFilterType() {
        return magFilterType;
    }

    /**
     * Sets the texture WRAP S type for texture.
     * 
     * @param wrapSType
     *            Basically, a GL constant that represents the type of wrap type
     *            one want to apply.
     */
    public void setWrapSType(TextureWrapType wrapSType) {
        this.wrapSType = wrapSType;
    }

    /**
     * Returns the texture WRAP S type of the {@link GVRTextureParameters}.
     * 
     * The returned value could be the one that was set by the calling API or
     * the default value for all the objects.
     * 
     * @return The texture WRAP S type as a TextureWrapType.
     */
    public TextureWrapType getWrapSType() {
        return wrapSType;
    }

    /**
     * Sets the texture WRAP T type for texture.
     * 
     * @param wrapTType
     *            Basically, a GL constant that represents the type of wrap type
     *            one want to apply.
     */
    public void setWrapTType(TextureWrapType wrapTType) {
        this.wrapTType = wrapTType;
    }

    /**
     * Returns the texture WRAP T type of the {@link GVRTextureParameters}.
     * 
     * The returned value could be the one that was set by the calling API or
     * the default value for all the objects.
     * 
     * @return The texture WRAP T type as a TextureWrapType.
     */
    public TextureWrapType getWrapTType() {
        return wrapTType;
    }

    /**
     * Returns true or false based on whether anisotropic filtering is supported
     * or not.
     * 
     * @return true or false based on whether anisotropic filtering is supported
     *         or not.
     */
    public boolean isAnisotropicSupported() {
        return mGVRContext.isAnisotropicSupported;
    }

    /**
     * Sets the anisotropic value. Normally the values that are set is 2, 4, 8
     * and 16.
     * 
     * So if someone tries to set a value which is greater than the max value,
     * 16, the method sets it to the max value.
     * 
     * Also the default value for anisotropic filter is 1, which means that it
     * is not applied when it is 1.
     * 
     * @param value
     *            An integer value from the set {2, 4, 8, 16}
     */
    public void setAnisotropicValue(int value) {
        if (value > getMaxAnisotropicValue()) {
            value = getMaxAnisotropicValue();
        }
        anisotropicValue = value;
    }

    /**
     * Returns the anisotropic value that was set by the calling API.
     * 
     * @return the anisotropic value that was set by the calling API.
     */
    public int getAnisotropicValue() {
        return anisotropicValue;
    }

    /**
     * Returns the maximum anisotropic value.
     * 
     * @return the maximum anisotropic value.
     */
    public int getMaxAnisotropicValue() {
        if (mGVRContext.isAnisotropicSupported) {
            return mGVRContext.maxAnisotropicValue;
        }
        return -1;
    }

    /**
     * Returns an integer array that contains the default values for all the
     * texture parameters.
     * 
     * @return an integer array that contains the default values for all the
     *         texture parameters.
     */
    public int[] getDefalutValuesArray() {
        int[] defaultValues = new int[5];

        defaultValues[0] = GLES20.GL_LINEAR; // MIN FILTER
        defaultValues[1] = GLES20.GL_LINEAR; // MAG FILTER
        defaultValues[2] = 1; // ANISO FILTER
        defaultValues[3] = GLES20.GL_CLAMP_TO_EDGE; // WRAP S
        defaultValues[4] = GLES20.GL_CLAMP_TO_EDGE; // WRAP T

        return defaultValues;
    }

    /**
     * Returns an integer array that contains the current values for all the
     * texture parameters.
     * 
     * @return an integer array that contains the current values for all the
     *         texture parameters.
     */
    public int[] getCurrentValuesArray() {
        int[] currentValues = new int[5];

        currentValues[0] = getMinFilterType().getFilterValue(); // MIN FILTER
        currentValues[1] = getMagFilterType().getFilterValue(); // MAG FILTER
        currentValues[2] = getAnisotropicValue(); // ANISO FILTER
        currentValues[3] = getWrapSType().getWrapValue(); // WRAP S
        currentValues[4] = getWrapTType().getWrapValue(); // WRAP T

        return currentValues;
    }

    /**
     * Enum values for all the filter types along with all its actual values.
     */
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

    /**
     * Enum values for all the wrap types along with all its actual values.
     */
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
    static native int getMaxAnisotropicValue();
}
