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

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.opengl.GLUtils;
import static android.opengl.GLES20.*;

/** Bitmap-based texture. */
public class GVRBitmapTexture extends GVRTexture {
    /**
     * Constructs a texture using a pre-existing {@link Bitmap}.
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     * @param bitmap
     *            A non-null {@link Bitmap} instance.
     */
    public GVRBitmapTexture(GVRContext gvrContext, Bitmap bitmap) {
        super(gvrContext, NativeBaseTexture.bitmapConstructor(bitmap));
    }

    /**
     * Constructs a texture by loading a bitmap from a PNG file in (or under)
     * the {@code assets} directory.
     * 
     * This method uses a native code path to create a texture directly from a
     * {@code .png} file; it does not create an Android {@link Bitmap}. It may
     * thus be slightly faster than loading a {@link Bitmap} and creating a
     * texture with {@link #GVRBaseTexture(GVRContext, Bitmap)}, and it should
     * reduce memory pressure, a bit.
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     * @param pngAssetFilename
     *            The name of a {@code .png} file, relative to the assets
     *            directory. The assets directory may contain an arbitrarily
     *            complex tree of subdirectories; the file name can specify any
     *            location in or under the assets directory.
     */
    public GVRBitmapTexture(GVRContext gvrContext, String pngAssetFilename) {
        super(gvrContext, NativeBaseTexture.fileConstructor(gvrContext
                .getContext().getAssets(), pngAssetFilename));
    }

    /**
     * Create a new, grayscale texture, from an array of luminance bytes.
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     * @param width
     *            Texture width, in pixels
     * @param height
     *            Texture height, in pixels
     * @param grayscaleData
     *            {@code width * height} bytes of gray scale data
     * 
     * @throws IllegalArgumentException
     *             If {@code width} or {@code height} is {@literal <= 0,} or if
     *             {@code grayScaleData} is {@code null}, or if
     *             {@code grayscaleData.length < height * width}
     * 
     * @since 1.6.3
     */
    public GVRBitmapTexture(GVRContext gvrContext, int width, int height,
            byte[] grayscaleData) throws IllegalArgumentException {
        super(gvrContext, NativeBaseTexture.bareConstructor());
        update(width, height, grayscaleData);
    }

    /**
     * Copy new luminance data to a grayscale texture.
     * 
     * Creating a new {@link GVRTexture} is pretty cheap, but it's still not a
     * totally trivial operation: it does involve some memory management and
     * some GL hardware handshaking. Reusing the texture reduces this overhead
     * (primarily by delaying garbage collection). Do be aware that updating a
     * texture will affect any and all {@linkplain GVRMaterial materials}
     * (and/or {@link GVRPostEffect post effects)} that use the texture!
     * 
     * @param width
     *            Texture width, in pixels
     * @param height
     *            Texture height, in pixels
     * @param grayscaleData
     *            {@code width * height} bytes of gray scale data
     * @return {@code true} if the update succeeded, and {@code false} if it
     *         failed. Updating a texture requires that the {@code bitmap}
     *         parameter has the exact same size and {@linkplain Config bit
     *         depth} as the original bitmap. In particular, you can't update a
     *         'normal' {@linkplain Config#ARGB_8888 32-bit} texture with
     *         grayscale data!
     * @throws IllegalArgumentException
     *             If {@code width} or {@code height} is {@literal <= 0,} or if
     *             {@code grayScaleData} is {@code null}, or if
     *             {@code grayscaleData.length < height * width}
     * 
     * @since 1.6.3
     */
    public boolean update(int width, int height, byte[] grayscaleData)
            throws IllegalArgumentException {
        if (width <= 0 || height <= 0 || grayscaleData == null
                || grayscaleData.length < height * width) {
            throw new IllegalArgumentException();
        }
        return NativeBaseTexture.update(getPtr(), width, height, grayscaleData);
    }

    /**
     * Copy a new {@link Bitmap} to the GL texture.
     * 
     * Creating a new {@link GVRTexture} is pretty cheap, but it's still not a
     * totally trivial operation: it does involve some memory management and
     * some GL hardware handshaking. Reusing the texture reduces this overhead
     * (primarily by delaying garbage collection). Do be aware that updating a
     * texture will affect any and all {@linkplain GVRMaterial materials}
     * (and/or {@link GVRPostEffect post effects)} that use the texture!
     * 
     * @param bitmap
     *            A standard Android {@link Bitmap}
     * @return {@code true} if the update succeeded, and {@code false} if it
     *         failed. Updating a texture requires that the {@code bitmap}
     *         parameter has the exact same size and {@linkplain Config bit
     *         depth} as the original bitmap. In particular, you can't update a
     *         grayscale texture with 'normal' {@linkplain Config#ARGB_8888
     *         32-bit} data!
     * 
     * @since 1.6.3
     */
    public boolean update(Bitmap bitmap) {
        glBindTexture(GL_TEXTURE_2D, getId());
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        return (glGetError() == GL_NO_ERROR);
    }
}

class NativeBaseTexture {
    static native long bitmapConstructor(Bitmap bitmap);

    static native long fileConstructor(AssetManager assetManager,
            String filename);

    static native long bareConstructor();

    static native boolean update(long pointer, int width, int height,
            byte[] grayscaleData);
}
