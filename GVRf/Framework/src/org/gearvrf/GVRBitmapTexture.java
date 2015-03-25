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
        super(gvrContext, NativeBaseTexture.ctor(bitmap));
    }

    /**
     * Constructs a texture by loading a bitmap from a PNG file in (or under)
     * the {@code assets} directory.
     * 
     * This method uses a native code path to create a texture directly from a
     * {@code .png} file; it does not create an Android {@link Bitmap}. It may thus be
     * slightly faster than loading a {@link Bitmap} and creating a texture with
     * {@link #GVRBaseTexture(GVRContext, Bitmap)}, and it should reduce memory
     * pressure, a bit.
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
        super(gvrContext, NativeBaseTexture.ctorWithFile(gvrContext
                .getContext().getAssets(), pngAssetFilename));
    }
}

class NativeBaseTexture {
    public static native long ctor(Bitmap bitmap);

    public static native long ctorWithFile(AssetManager assetManager,
            String filename);
}
