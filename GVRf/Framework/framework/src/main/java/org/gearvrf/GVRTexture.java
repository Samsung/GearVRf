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

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import org.gearvrf.asynchronous.GVRCompressedTexture;
import org.gearvrf.utility.Log;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static android.opengl.GLES20.GL_LINEAR_MIPMAP_NEAREST;
import static android.opengl.GLES20.GL_NO_ERROR;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glGetError;
import static android.opengl.GLES20.glTexParameteri;

/** Wrapper for a GL texture. */
public class GVRTexture extends GVRHybridObject implements GVRAndroidResource.TextureCallback
{
    protected static final String TAG = "GVRTexture";
    protected GVRImage  mImage;
    protected GVRTextureParameters mTextureParams;
    private volatile int mTextureId;
    private final ReentrantLock mLock;
    private final Condition mCondition;

    public GVRTexture(GVRContext gvrContext)
    {
        super(gvrContext, NativeTexture.constructor());
        mImage = null;
        mLock = new ReentrantLock();
        mCondition = mLock.newCondition();
        mTextureParams = null;
        mTextureId = 0;
    }

    protected GVRTexture(GVRContext gvrContext, long ptr)
    {
        super(gvrContext, ptr);
        mImage = null;
        mLock = new ReentrantLock();
        mCondition = mLock.newCondition();
        mTextureParams = null;
        mTextureId = 0;
    }

    public GVRTexture(GVRContext gvrContext, GVRTextureParameters texparams)
    {
        super(gvrContext, NativeTexture.constructor());
        mImage = null;
        mTextureId = 0;
        mLock = new ReentrantLock();
        mCondition = mLock.newCondition();
        updateTextureParameters(texparams);
    }

    public boolean stillWanted(GVRAndroidResource r)
    {
        return true;
    }

    public void loaded(GVRImage image, GVRAndroidResource resource)
    {
        setImage(image);
    }

    public void failed(Throwable ex, GVRAndroidResource resource) { }

    public int getId()
    {
        if (mTextureId != 0)
        {
            return mTextureId;
        }

        final CountDownLatch cdl = new CountDownLatch(1);
        getGVRContext().runOnGlThread(new Runnable() {
            @Override
            public void run() {
                mTextureId = NativeTexture.getId(getNative());
                cdl.countDown();
            }
        });
        try
        {
            cdl.await();
        }
        catch (final Exception exc)
        {
            throw new IllegalStateException("Exception waiting for texture ready");
        }
        return mTextureId;
    }


    /**
     * Update the texture parameters {@link GVRTextureParameters} after the
     * texture has been created.
     */
    public void updateTextureParameters(GVRTextureParameters textureParameters)
    {
        mTextureParams = textureParameters;
        long nativePtr = getNative();
        if (nativePtr != 0)
        {
            NativeTexture.updateTextureParameters(nativePtr, textureParameters.getCurrentValuesArray());
        }
    }

    /**
     * Returns the list of atlas information necessary to map
     * the texture atlas to each scene object.
     *
     * @return List of atlas information.
     */
    public List<GVRAtlasInformation> getAtlasInformation()
    {
        if  (mImage == null)
        {
            return null;
        }
        return mImage.getAtlasInformation();
    }

    /**
     * Set the list of {@link GVRAtlasInformation} to map the texture atlas
     * to each object of the scene.
     *
     * @param atlasInformation Atlas information to map the texture atlas to each
     *        scene object.
     */
    public void setAtlasInformation(List<GVRAtlasInformation> atlasInformation)
    {
        if (mImage != null)
        {
            mImage.setAtlasInformation(atlasInformation);
        }
     }

    /**
     * Inform if the texture is a large image containing "atlas" of sub-images
     * with a list of {@link GVRAtlasInformation} necessary to map it to the
     * scene objects.
     *
     * @return True if the texture is a large image containing "atlas",
     *         otherwise it returns false.
     */
    public boolean isAtlasedTexture()
    {
        return (mImage != null) && mImage.isAtlasedTexture();
    }

    /**
     * Changes the image data associated with a GVRTexture.
     * This can be a simple bitmap, a compressed bitmap,
     * a cubemap or a compressed cubemap.
     * @param imageData data for the texture as a GVRImate
     */
    public void setImage(final GVRImage imageData)
    {
        if (imageData instanceof GVRBitmapTexture)
        {
            update((GVRBitmapTexture) imageData);
        }
        else if (imageData instanceof GVRCompressedTexture)
        {
            update((GVRCompressedTexture) imageData);
        }
        else if (imageData instanceof GVRCubemapTexture)
        {
            update((GVRCubemapTexture) imageData);
        }
        else if (imageData instanceof GVRCompressedCubemapTexture)
        {
            update((GVRCompressedCubemapTexture) imageData);
        }
        else
        {
            throw new UnsupportedOperationException("GVRTexture.setImage unsupported image class");
        }
        mImage = imageData;
        if (imageData != null)
            NativeTexture.setImage(getNative(), imageData, imageData.getNative());
        else
            NativeTexture.setImage(getNative(), null, 0);
    }

    public GVRImage getImage()
    {
        return mImage;
    }

    /**
     * Copy a new {@link Bitmap} to the GL texture. This one is also safe even
     * in a non-GL thread. An update request on a non-GL thread will
     * be forwarded to the GL thread and be executed before main rendering happens.
     *
     * Creating a new {@link GVRImage} is pretty cheap, but it's still not a
     * totally trivial operation: it does involve some memory management and
     * some GL hardware handshaking. Reusing the texture reduces this overhead
     * (primarily by delaying garbage collection). Do be aware that updating a
     * texture will affect any and all {@linkplain GVRMaterial materials}
     * (and/or post effects that use the texture!
     *
     * @param bitmapImage
     *            A GVRBitmapTexture containing an Android Bitmap or grayscale data.
     *
     * @since 1.6.3
     */
    public void update(final GVRBitmapTexture bitmapImage)
    {
        final Bitmap bitmap = bitmapImage.getBitmap();
        final int width = bitmapImage.getWidth();
        final int height = bitmapImage.getHeight();
        final int format = bitmapImage.getFormat();
        final byte[] grayscale = bitmapImage.getGrayscale();

        if ((width <= 0) || (height <= 0) ||
            ((grayscale != null) && (grayscale.length < height * width)))
        {
            throw new IllegalArgumentException();
        }
        long nativePtr = bitmapImage.getNative();
        if (nativePtr == 0)
        {
            nativePtr = NativeBitmapImage.constructor(width, height, grayscale);
            bitmapImage.setNative(nativePtr);
        }
        if (bitmap != null)
        {
            NativeBitmapImage.updateFromBitmap(nativePtr, bitmap);
        }
        else if (grayscale != null)
        {
            NativeBitmapImage.updateFromMemory(nativePtr, width, height, grayscale);
        }
    }

    void update(final GVRCubemapTexture cubemap)
    {
        long nativePtr = cubemap.getNative();
        Bitmap[] bitmapArray = cubemap.getFaceBitmaps();
        if (nativePtr == 0)
        {
            nativePtr = NativeCubemapImage.bitmapArrayConstructor(bitmapArray);
            cubemap.setNative(nativePtr);
        }
        else
        {
            NativeCubemapImage.update(nativePtr, bitmapArray);
        }
    }

    void update(final GVRCompressedCubemapTexture cubemap)
    {
        long nativePtr = cubemap.getNative();
        if (nativePtr == 0)
        {
            nativePtr = NativeCompressedCubemapTexture.constructor(
                    cubemap.getFormat(), cubemap.getWidth(), cubemap.getHeight(),
                    cubemap.getImageSize(), cubemap.getData(), cubemap.getDataOffsets());
            cubemap.setNative(nativePtr);
        }
        else
        {
            NativeCompressedCubemapTexture.update(nativePtr, cubemap.getData(), cubemap.getDataOffsets());
        }
    }

    void update(final GVRCompressedTexture texture)
    {
        long nativePtr = texture.getNative();
        int levels = texture.getLevels();
        if (nativePtr == 0)
        {
            nativePtr = NativeCompressedTexture.constructor(texture.getTarget(), texture.getFormat(),
                    texture.getWidth(), texture.getHeight(),
                    texture.getData(), texture.getDataOffsets());
            texture.setNative(nativePtr);
            return;
        }
        NativeCompressedTexture.update(nativePtr, texture.getWidth(), texture.getHeight(),
                texture.getData(), texture.getDataOffsets());
    }
}

class NativeTexture {
    static native long constructor();
    static native int getId(long texture);
    static native boolean isReady(long texture);
    static native void updateTextureParameters(long texture, int[] textureParametersValues);
    static native void setImage(long texPointer, GVRImage javeImage, long nativeImage);
}

class NativeCubemapImage
{
    static native long bitmapArrayConstructor(Bitmap[] bitmapArray);
    static native void update(long pointer, Bitmap[] bitmapArray);
}

class NativeCompressedCubemapTexture {
    static native long constructor(int internalFormat,
                                   int width, int height, int imageSize, byte[][] data, int[] dataOffsets);
    static native void update(long pointer, byte[][] data, int[] dataOffsets);
}

class NativeCompressedTexture {
    static native long constructor(int target, int format,
                                   int width, int height, byte[] data, int[] dataOffsets);

    static native void update(long pointer, int width, int height, byte[] data, int[] dataOffsets);
}

class NativeBitmapImage {
    static native long constructor(int width, int height, byte[] data);

    static native void updateFromMemory(long pointer, int width, int height, byte[] data);
    static native void updateFromBitmap(long pointer, Bitmap bitmap);
}