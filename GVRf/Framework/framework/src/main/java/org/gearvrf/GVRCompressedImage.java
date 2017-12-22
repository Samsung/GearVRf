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

import org.gearvrf.utility.Log;

/**
 * Describes a compressed bitmap texture.
 * <p>
 * A compressed texture contains 2D compressed pixel data.
 * This type of texture is very efficient because
 * it uses less memory. Mobile GPUs can directly render from
 * compressed textures.
 * @see GVRBitmapeTexture
 */
public class GVRCompressedImage extends GVRImage
{
    private static final String TAG = Log.tag(GVRCompressedImage.class);

    /**
     * The speed/quality parameter passed to
     * {@link GVRAssetLoader#loadTexture(GVRAndroidResource, GVRAndroidResource.TextureCallback, GVRTextureParameters, int, int)}
     * 
     * This copy has been 'clamped' to one of the
     * {@linkplain GVRCompressedImage#SPEED public constants} in
     * {@link GVRCompressedImage}.
     */
    protected int mQuality;
    private byte[] mData;
    private int mWidth;
    private int mHeight;
    private int mLevels;
    private int mImageSize;

    /**
     * Create a compressed texture.
     * @param gvrContext    GVRContext to use for texture.
     * @param width         pixel width of image.
     * @param height        pixel height of image.
     * @param imageSize     number of bytes in compressed image data.
     * @param format        image format (GL_RGB, GL_RGBA, ...)
     * @param data          image data bytes
     * @param levels        number of mip-map levels
     * @param quality       compression quality
     */
    public GVRCompressedImage(GVRContext gvrContext, int width, int height, int imageSize, int format, byte[] data, int levels, int quality)
    {
        super(gvrContext, NativeBitmapImage.constructor(ImageType.BITMAP.Value, format));
        mQuality = GVRCompressedImage.clamp(quality);
        mData = data;
        mWidth = width;
        mHeight = height;
        mLevels = levels;
        mImageSize = imageSize;
    }

    /**
     * Set the offsets in the compressed data area for each mip-map level.
     * @param offsets array of offsets
     */
    public void setDataOffsets(int[] offsets)
    {
        assert(mLevels == offsets.length);
        NativeBitmapImage.updateCompressed(getNative(), mWidth, mHeight, mImageSize, mData, mLevels, offsets);
        mData = null;
    }

    /**
     * Get compression quality
     * @return compression quality
     */
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
