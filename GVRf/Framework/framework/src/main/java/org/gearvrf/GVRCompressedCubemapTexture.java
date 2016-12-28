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

public class GVRCompressedCubemapTexture extends GVRImage
{
    protected int mWidth;
    protected int mHeight;
    protected int mImageSize;

    public GVRCompressedCubemapTexture(GVRContext gvrContext, int internalFormat, int width,
                                        int height, int imageSize, byte[][] data, int[] dataOffsets)
    {
        super(gvrContext, NativeBitmapImage.constructor(ImageType.CUBEMAP.Value, internalFormat));
        mWidth = width;
        mHeight = height;
        mImageSize = imageSize;
        NativeCubemapImage.updateCompressed(getNative(), width, height, imageSize, data, dataOffsets);
    }

    public void update(byte[][] data, int[] dataOffsets)
    {
        NativeCubemapImage.updateCompressed(getNative(), mWidth, mHeight, mImageSize, data, dataOffsets);
    }
}
