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

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAndroidResource.BitmapTextureCallback;
import org.gearvrf.GVRAndroidResource.CompressedTextureCallback;
import org.gearvrf.GVRAndroidResource.MeshCallback;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.utility.Threads;

import android.graphics.Bitmap;

/**
 * Internal API for asynchronous resource loading.
 * 
 * You will normally call into this class through
 * {@link GVRContext#loadCompressedTexture(GVRAndroidResource.CompressedTextureCallback, GVRAndroidResource)}
 * or
 * {@link GVRContext#loadBitmapTexture(GVRAndroidResource.BitmapTextureCallback, GVRAndroidResource)}
 * or
 * {@link GVRContext#loadMesh(GVRAndroidResource.MeshCallback, GVRAndroidResource)}
 * .
 * 
 * @since 1.6.1
 */
public class GVRAsynchronousResourceLoader {

    /**
     * Get device parameters and so on.
     * 
     * This is an internal method, public only so it can be called across
     * package boundaries. Calling it from user code is both harmless and
     * pointless.
     */
    public static void setup(GVRContext gvrContext) {
        AsyncBitmapTexture.setup(gvrContext);
    }

    /**
     * Load a compressed texture asynchronously.
     * 
     * This is the implementation of
     * {@link GVRContext#loadCompressedTexture(GVRAndroidResource.CompressedTextureCallback, GVRAndroidResource)}
     * : it will usually be more convenient to call that directly.
     * 
     * @param gvrContext
     *            The GVRF context
     * @param callback
     *            Asynchronous notifications
     * @param resource
     *            Stream containing a compressed texture
     * @throws IllegalArgumentException
     *             If {@code gvrContext} or {@code callback} parameters are
     *             {@code null}
     */
    public static void loadCompressedTexture(final GVRContext gvrContext,
            final CompressedTextureCallback callback,
            final GVRAndroidResource resource) throws IllegalArgumentException {
        loadCompressedTexture(gvrContext, callback, resource,
                GVRCompressedTexture.DEFAULT_QUALITY);
    }

    /**
     * Load a compressed texture asynchronously.
     * 
     * This is the implementation of
     * {@link GVRContext#loadCompressedTexture(GVRAndroidResource.CompressedTextureCallback, GVRAndroidResource)}
     * : it will usually be more convenient to call that directly.
     * 
     * @param gvrContext
     *            The GVRF context
     * @param callback
     *            Asynchronous notifications
     * @param resource
     *            Basically, a stream containing a compressed texture. Taking a
     *            {@link GVRAndroidResource} parameter eliminates six overloads.
     * @param quality
     *            Speed/quality tradeoff: should be one of
     *            {@link GVRCompressedTexture#SPEED},
     *            {@link GVRCompressedTexture#BALANCED}, or
     *            {@link GVRCompressedTexture#QUALITY}, but other values are
     *            'clamped' to one of the recognized values.
     * @throws IllegalArgumentException
     *             If {@code gvrContext} or {@code callback} parameters are
     *             {@code null}
     */
    public static void loadCompressedTexture(final GVRContext gvrContext,
            final CompressedTextureCallback callback,
            final GVRAndroidResource resource, final int quality)
            throws IllegalArgumentException {
        validateCallbackParameters(gvrContext, callback, resource);

        // Load the bytes on a background thread
        Threads.spawn(new Runnable() {

            @Override
            public void run() {
                try {
                    final CompressedTexture compressedTexture = CompressedTexture
                            .load(resource.getStream(), false);
                    resource.closeStream();
                    // Create texture on GL thread
                    gvrContext.runOnGlThread(new Runnable() {

                        @Override
                        public void run() {
                            GVRTexture texture = compressedTexture.toTexture(
                                    gvrContext, quality);
                            callback.loaded(texture, resource);
                        }
                    });
                } catch (Exception e) {
                    callback.failed(e, resource);
                }
            }
        });
    }

    /**
     * Load a bitmap texture asynchronously.
     * 
     * This is the implementation of
     * {@link GVRContext#loadBitmapTexture(GVRAndroidResource.BitmapTextureCallback, GVRAndroidResource, int)}
     * - it will usually be more convenient to call that directly.
     * 
     * @param gvrContext
     *            The GVRF context
     * @param callback
     *            Asynchronous notifications
     * @param resource
     *            Basically, a stream containing a compressed texture. Taking a
     *            {@link GVRAndroidResource} parameter eliminates six overloads.
     * @param priority
     *            A value {@literal >=} {@link GVRContext#LOWEST_PRIORITY} and
     *            {@literal <=} {@link GVRContext#HIGHEST_PRIORITY}
     * @throws IllegalArgumentException
     *             If {@code priority} {@literal <}
     *             {@link GVRContext#LOWEST_PRIORITY} or {@literal >}
     *             {@link GVRContext#HIGHEST_PRIORITY}, or any of the other
     *             parameters are {@code null}.
     */
    public static void loadBitmapTexture(GVRContext gvrContext,
            BitmapTextureCallback callback, GVRAndroidResource resource,
            int priority) throws IllegalArgumentException {
        validatePriorityCallbackParameters(gvrContext, callback, resource,
                priority);

        AsyncBitmapTexture
                .loadTexture(gvrContext, callback, resource, priority);
    }

    /**
     * 
     * Load a GL mesh asynchronously.
     * 
     * This is the implementation of
     * {@link GVRContext#loadMesh(GVRAndroidResource.MeshCallback, GVRAndroidResource, int)}
     * - it will usually be more convenient to call that directly.
     * 
     * @param gvrContext
     *            The GVRF context
     * @param callback
     *            Asynchronous notifications
     * @param resource
     *            Basically, a stream containing a 3D model. Taking a
     *            {@link GVRAndroidResource} parameter eliminates six overloads.
     * @param priority
     *            A value {@literal >=} {@link GVRContext#LOWEST_PRIORITY} and
     *            {@literal <=} {@link GVRContext#HIGHEST_PRIORITY}
     * @throws IllegalArgumentException
     *             If {@code priority} {@literal <}
     *             {@link GVRContext#LOWEST_PRIORITY} or {@literal >}
     *             {@link GVRContext#HIGHEST_PRIORITY}, or any of the other
     *             parameters are {@code null}.
     * 
     * @since 1.6.2
     */
    public static void loadMesh(GVRContext gvrContext, MeshCallback callback,
            GVRAndroidResource resource, int priority) {
        validatePriorityCallbackParameters(gvrContext, callback, resource,
                priority);

        AsyncMesh.loadMesh(gvrContext, callback, resource, priority);
    }

    private static <T extends GVRHybridObject> void validateCallbackParameters(
            GVRContext gvrContext, GVRAndroidResource.Callback<T> callback,
            GVRAndroidResource resource) {
        if (gvrContext == null) {
            throw new IllegalArgumentException("gvrContext == null");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback == null");
        }
        if (resource == null) {
            throw new IllegalArgumentException("resource == null");
        }
    }

    private static <T extends GVRHybridObject> void validatePriorityCallbackParameters(
            GVRContext gvrContext, GVRAndroidResource.Callback<T> callback,
            GVRAndroidResource resource, int priority) {
        validateCallbackParameters(gvrContext, callback, resource);
        if (priority < GVRContext.LOWEST_PRIORITY
                || priority > GVRContext.HIGHEST_PRIORITY) {
            throw new IllegalArgumentException(
                    "Priority < GVRContext.LOWEST_PRIORITY or > GVRContext.HIGHEST_PRIORITY");
        }
    }

    /**
     * An internal method, public only so that GVRContext can make cross-package
     * calls.
     * 
     * A wrapper around
     * {@link android.graphics.BitmapFactory#decodeStream(InputStream)
     * BitmapFactory.decodeStream} that uses an
     * {@link android.graphics.BitmapFactory.Options} <code>inTempStorage</code>
     * decode buffer. On low memory, returns half (quarter, eighth, ...) size
     * images.
     * <p>
     * If {@code stream} is a {@link FileInputStream} and is at offset 0 (zero),
     * uses
     * {@link android.graphics.BitmapFactory#decodeFileDescriptor(FileDescriptor)
     * BitmapFactory.decodeFileDescriptor()} instead of
     * {@link android.graphics.BitmapFactory#decodeStream(InputStream)
     * BitmapFactory.decodeStream()}.
     * 
     * @param stream
     *            Bitmap stream
     * @param closeStream
     *            If {@code true}, closes {@code stream}
     * @return Bitmap, or null if cannot be decoded into a bitmap
     */
    public static Bitmap decodeStream(InputStream stream, boolean closeStream) {
        return AsyncBitmapTexture.decodeStream(stream,
                AsyncBitmapTexture.glMaxTextureSize,
                AsyncBitmapTexture.glMaxTextureSize, true, null, closeStream);
    }
}
