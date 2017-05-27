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

import static android.opengl.GLES20.*;

import org.gearvrf.utility.Log;

/**
 * A GL compressed texture; you get it from
 * {@linkplain GVRAssetLoader#loadTexture(org.gearvrf.GVRAndroidResource.TextureCallback, org.gearvrf.GVRAndroidResource)
 * GVRContext.loadCompressedTexture()}.
 * 
 * This is mostly an internal, implementation class: You <em>may</em> find
 * {@link #mLevels} and/or {@link #mQuality} useful.
 * 
 * @since 1.6.1
 */
public class GVRCompressedTexture extends GVRImage
{
    private static final String TAG = Log.tag(GVRCompressedTexture.class);

    /**
     * The speed/quality parameter passed to
     * {@link GVRAssetLoader#loadTexture(org.gearvrf.GVRAndroidResource.TextureCallback, org.gearvrf.GVRAndroidResource, int)
     * GVRContext.loadCompressedTexture()}.
     * 
     * This copy has been 'clamped' to one of the
     * {@linkplain GVRCompressedTexture#SPEED public constants} in
     * {@link GVRCompressedTexture}.
     */
    protected int mQuality;
    private byte[] mData;
    private int mWidth;
    private int mHeight;
    private int mLevels;
    private int mImageSize;

    // Texture parameters
    public GVRCompressedTexture(GVRContext gvrContext, int width, int height,  int imageSize, int format, byte[] data, int levels, int quality)
    {
        super(gvrContext, NativeBitmapImage.constructor(ImageType.BITMAP.Value, format));
        mQuality = GVRCompressedTexture.clamp(quality);
        mData = data;
        mWidth = width;
        mHeight = height;
        mLevels = levels;
        mImageSize = imageSize;
    }


    public void setDataOffsets(int[] offsets)
    {
        assert(mLevels == offsets.length);
        NativeBitmapImage.updateCompressed(getNative(), mWidth, mHeight, mImageSize, mData, mLevels, offsets);
        mData = null;
    }

    public int getQuality()         { return mQuality; }

     /*
     * Quality tradeoff constants
     */

    /** Optimize for updateGPU speed */
    public static final int SPEED = -1;
    /** Strike a balance between speed and quality */
    public static final int BALANCED = 0;
    /** Optimize for updateGPU quality */
    public static final int QUALITY = 1;

    public static final int DEFAULT_QUALITY = SPEED;

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
