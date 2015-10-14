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

package org.gearvrf.asynchronous;

import static android.opengl.GLES20.*;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.utility.Log;
import org.gearvrf.utility.RuntimeAssertion;

/**
 * A GL compressed texture; you get it from
 * {@linkplain GVRContext#loadCompressedTexture(org.gearvrf.GVRAndroidResource.CompressedTextureCallback, org.gearvrf.GVRAndroidResource)
 * GVRContext.loadCompressedTexture()}.
 * 
 * This is mostly an internal, implementation class: You <em>may</em> find
 * {@link #mLevels} and/or {@link #mQuality} useful.
 * 
 * @since 1.6.1
 */
public class GVRCompressedTexture extends GVRTexture {

    static final int GL_TARGET = GL_TEXTURE_2D;

    private static final String TAG = Log.tag(GVRCompressedTexture.class);

    /*
     * Texture field(s) and constructors
     */

    /**
     * Number of texture levels. 1 means a single image, with no mipmap chain;
     * values higher than 1 mean the texture has a mipmap chain.
     */
    public final int mLevels;

    /**
     * The speed/quality parameter passed to
     * {@link GVRContext#loadCompressedTexture(org.gearvrf.GVRAndroidResource.CompressedTextureCallback, org.gearvrf.GVRAndroidResource, int)
     * GVRContext.loadCompressedTexture()}.
     * 
     * This copy has been 'clamped' to one of the
     * {@linkplain GVRCompressedTexture#SPEED public constants} in
     * {@link GVRCompressedTexture}.
     */
    public final int mQuality;

    GVRCompressedTexture(GVRContext gvrContext, int internalFormat, int width,
            int height, int imageSize, byte[] data, int dataOffset,
            int levels, int quality) {
        this(gvrContext, internalFormat, width, height, imageSize, data, dataOffset,
                levels, quality, gvrContext.DEFAULT_TEXTURE_PARAMETERS);
    }

    // Texture parameters
    GVRCompressedTexture(GVRContext gvrContext, int internalFormat, int width,
            int height, int imageSize, byte[] data, int dataOffset,
            int levels, int quality,
            GVRTextureParameters textureParameters) {
        super(gvrContext, NativeCompressedTexture.normalConstructor(GL_TARGET,
                internalFormat, width, height, imageSize, data, dataOffset,
                textureParameters.getCurrentValuesArray()));
        mLevels = levels;
        mQuality = GVRCompressedTexture.clamp(quality);

        updateMinification();
    }

    GVRCompressedTexture(GVRContext gvrContext, int target, int levels,
            int quality) {
        super(gvrContext, NativeCompressedTexture.mipmappedConstructor(target));
        mLevels = levels;
        mQuality = GVRCompressedTexture.clamp(quality);

        updateMinification();
    }

    private void updateMinification() {
        boolean rebound = true; // in 2 out of 3 branches ...
        if (mLevels > 1) {
            rebind();
            glTexParameteri(GL_TARGET, GL_TEXTURE_MIN_FILTER,
                    selectMipMapMinification(mQuality));
        } else if (mQuality == QUALITY) {
            Log.d(TAG, "quality == %s, GL_TEXTURE_MIN_FILTER = %s", "QUALITY",
                    "GL_LINEAR");
            rebind();
            glTexParameteri(GL_TARGET, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        } else {
            rebound = false;
        }
        if (rebound) {
            unbind();
        }
    }

    private static int selectMipMapMinification(int quality) {
        switch (quality) {
        case SPEED:
            Log.d(TAG, "quality == %s, GL_TEXTURE_MIN_FILTER = %s", "SPEED",
                    "GL_NEAREST_MIPMAP_NEAREST");
            return GL_NEAREST_MIPMAP_NEAREST;
        case BALANCED:
            Log.d(TAG, "quality == %s, GL_TEXTURE_MIN_FILTER = %s", "BALANCED",
                    "GL_LINEAR_MIPMAP_NEAREST");
            return GL_LINEAR_MIPMAP_NEAREST;
        case QUALITY:
            Log.d(TAG, "quality == %s, GL_TEXTURE_MIN_FILTER = %s", "QUALITY",
                    "GL_LINEAR_MIPMAP_LINEAR");
            return GL_LINEAR_MIPMAP_LINEAR;
        default:
            throw new RuntimeAssertion(
                    "The quality parameter should have been clamped");
        }
    }

    protected void rebind() {
        glBindTexture(GL_TARGET, getId());
    }

    protected void unbind() {
        glBindTexture(GL_TARGET, 0);
    }

    /*
     * Quality tradeoff constants
     */

    /** Optimize for render speed */
    public static final int SPEED = -1;
    /** Strike a balance between speed and quality */
    public static final int BALANCED = 0;
    /** Optimize for render quality */
    public static final int QUALITY = 1;

    protected static final int DEFAULT_QUALITY = SPEED;

    private static int clamp(int quality) {
        if (quality < 0) {
            return SPEED;
        } else if (quality > 0) {
            return QUALITY;
        } else {
            return BALANCED;
        }
    }
}

class NativeCompressedTexture {
    static native long normalConstructor(int target, int internalFormat,
            int width, int height, int imageSize, byte[] data, int dataOffset,
            int[] textureParameterValues);

    static native long mipmappedConstructor(int target);
}