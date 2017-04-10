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

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

import org.gearvrf.utility.Log;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;

/** Bitmap-based texture. */
public class GVRBitmapTexture extends GVRTexture {
    /**
     * Constructs a texture using a pre-existing {@link Bitmap}.
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     * @param bitmap
     *            A non-null {@link Bitmap} instance; do *not* call
     *            recycle on the bitmap
     */
    public GVRBitmapTexture(GVRContext gvrContext, Bitmap bitmap) {
        this(gvrContext, bitmap, gvrContext.DEFAULT_TEXTURE_PARAMETERS);
    }

    /**
     * Constructs a texture using a pre-existing {@link Bitmap} and the user
     * defined filters {@link GVRTextureParameters}.
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     * @param bitmap
     *            A non-null {@link Bitmap} instance; do *not* call
     *            recycle on the bitmap
     * @param textureParameters
     *            User defined object for {@link GVRTextureParameters} which may
     *            also contain default values.
     */
    public GVRBitmapTexture(GVRContext gvrContext, Bitmap bitmap,
            GVRTextureParameters textureParameters) {
        super(gvrContext, NativeBaseTexture.bareConstructor(textureParameters.getCurrentValuesArray()));
        NativeBaseTexture.setJavaOwner(getNative(), this);
        mBitmap = bitmap;
    }

    /**
     * Constructs a texture by loading a bitmap from a PNG file in (or under)
     * the {@code assets} directory.
     * 
     * This method uses a native code path to create a texture directly from a
     * {@code .png} file; it does not create an Android {@link Bitmap}. It may
     * thus be slightly faster than loading a {@link Bitmap} and creating a
     * texture with {@link #GVRBitmapTexture(GVRContext, Bitmap)}, and it should
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
        this(gvrContext, pngAssetFilename,
                gvrContext.DEFAULT_TEXTURE_PARAMETERS);
    }

    /**
     * Constructs a texture by loading a bitmap from a PNG file in (or under)
     * the {@code assets} directory and the user defined filters
     * {@link GVRTextureParameters}.
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     * @param pngAssetFilename
     *            The name of a {@code .png} file, relative to the assets
     *            directory. The assets directory may contain an arbitrarily
     *            complex tree of subdirectories; the file name can specify any
     *            location in or under the assets directory.
     * @param textureParameters
     *            User defined object for {@link GVRTextureParameters} which may
     *            also contain default values.
     */
    public GVRBitmapTexture(GVRContext gvrContext, String pngAssetFilename,
            GVRTextureParameters textureParameters) {
        this(gvrContext, getBitmap(gvrContext, pngAssetFilename), textureParameters);
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
        this(gvrContext, width, height, grayscaleData,
                gvrContext.DEFAULT_TEXTURE_PARAMETERS);
    }

    /**
     * Create a new, grayscale texture, from an array of luminance bytes and the
     * user defined filters {@link GVRTextureParameters}.
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     * @param width
     *            Texture width, in pixels
     * @param height
     *            Texture height, in pixels
     * @param grayscaleData
     *            {@code width * height} bytes of gray scale data
     * @param textureParameters
     *            User defined object for {@link GVRTextureParameters} which may
     *            also contain default values.
     * 
     * @throws IllegalArgumentException
     *             If {@code width} or {@code height} is {@literal <= 0,} or if
     *             {@code grayScaleData} is {@code null}, or if
     *             {@code grayscaleData.length < height * width}
     */
    public GVRBitmapTexture(GVRContext gvrContext, int width, int height,
            byte[] grayscaleData, GVRTextureParameters textureParameters)
            throws IllegalArgumentException {
        super(gvrContext, NativeBaseTexture.bareConstructor(textureParameters
                .getCurrentValuesArray()));
        NativeBaseTexture.setJavaOwner(getNative(), this);
        mWidth = width;
        mHeight = height;
        mGrayscaleData = grayscaleData;
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
    private boolean updateCall(int width, int height, byte[] grayscaleData)
            throws IllegalArgumentException {
        if (width <= 0 || height <= 0 || grayscaleData == null
                || grayscaleData.length < height * width) {
            throw new IllegalArgumentException();
        }
        return NativeBaseTexture.update(getNative(), width, height,
                grayscaleData);
    }

    /**
     * Copy new luminance data to a grayscale texture. This is also safe to be
     * called in a non-GL thread.
     * 
     * @param width
     *            Texture width, in pixels
     * @param height
     *            Texture height, in pixels
     * @param grayscaleData
     *            {@code width * height} bytes of gray scale data
     * @return {@link Future<Boolean>} A update request on a non-GL thread will
     *         finally be forwarded to the GL thread and be executed before main
     *         rendering happens. So at the time we call the safeUpdate, we can
     *         only return a Future containing a boolean value to see if it is
     *         successfully updated later in GL thread.
     * @since 1.6.3
     */
    public Future<Boolean> update(int width, int height, byte[] grayscaleData) {
        final int widthOnCall = width, heightOnCall = height;
        final byte[] grayscaleDataOnCall = grayscaleData;
        RunnableFuture<Boolean> updateTask = new GVRFutureOnGlThread<Boolean>(
                new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        return updateCall(widthOnCall, heightOnCall,
                                grayscaleDataOnCall);
                    }
                });
        if (getGVRContext().isCurrentThreadGLThread()) {
            updateTask.run();
        } else {
            getGVRContext().runOnGlThread(updateTask);
        }
        return updateTask;
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

    private boolean updateCall(Bitmap bitmap) {
        glBindTexture(GL_TEXTURE_2D, getId());
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        glGenerateMipmap(GL_TEXTURE_2D);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);        
        return (glGetError() == GL_NO_ERROR);
    }

    /**
     * Copy a new {@link Bitmap} to the GL texture. This one is also safe even
     * in a non-GL thread.
     *
     * Creating a new {@link GVRTexture} is pretty cheap, but it's still not a
     * totally trivial operation: it does involve some memory management and
     * some GL hardware handshaking. Reusing the texture reduces this overhead
     * (primarily by delaying garbage collection). Do be aware that updating a
     * texture will affect any and all {@linkplain GVRMaterial materials}
     * (and/or {@link GVRPostEffect post effects)} that use the texture!
     * 
     * @param bitmap
     *            A standard Android {@link Bitmap} gvrContext Current GVR
     *            context we are running with.
     * @return {@link Future<Boolean>} A update request on a non-GL thread will
     *         finally be forwarded to the GL thread and be executed before main
     *         rendering happens. So at the time we call the safeUpdate, we can
     *         only return a Future containing a boolean value to see if it is
     *         successfully updated later in GL thread.
     * 
     * @since 1.6.3
     */
    public Future<Boolean> update(Bitmap bitmap) {
        final Bitmap onCallBitmap = bitmap;
        RunnableFuture<Boolean> updateTask = new GVRFutureOnGlThread<Boolean>(
                new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        return updateCall(onCallBitmap);
                    }
                });
        if (getGVRContext().isCurrentThreadGLThread()) {
            updateTask.run();
        } else {
            getGVRContext().runOnGlThread(updateTask);
        }
        return updateTask;
    }

    private static Bitmap getBitmap(GVRContext gvrContext, String pngAssetFilename) {
        try {
            return BitmapFactory.decodeStream(
                    gvrContext.getContext().getAssets().open(pngAssetFilename));
        } catch (final IOException exc) {
            Log.e(TAG, "asset not found", exc);
        }
        return null;
    }

    @SuppressWarnings("unused")
    protected void idAvailable(final int id) {
        super.idAvailable(id);

        if (null != mBitmap) {
            updateCall(mBitmap);
            mBitmap = null;
        } else if (null != mGrayscaleData) {
            updateCall(mWidth, mHeight, mGrayscaleData);
            mGrayscaleData = null;
        }
    }

    private Bitmap mBitmap;
    private int mWidth;
    private int mHeight;
    private byte[] mGrayscaleData;
    private final static String TAG = "GVRBitmapTexture";
}

final class NativeBaseTexture {
    static native long bareConstructor(int[] textureParameterValues);
    static native void setJavaOwner(long pointer, GVRTexture owner);

    static native boolean update(long pointer, int width, int height,
            byte[] grayscaleData);
}
