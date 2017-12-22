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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.gearvrf.GVRCompressedImage;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAndroidResource.CancelableCallback;
import org.gearvrf.GVRAndroidResource.CompressedTextureCallback;
import org.gearvrf.GVRAndroidResource.TextureCallback;
import org.gearvrf.GVRAtlasInformation;
import org.gearvrf.GVRBitmapImage;
import org.gearvrf.GVRCompressedCubemapImage;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCubemapImage;
import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRImage;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRTexture;
import org.gearvrf.utility.Log;
import org.gearvrf.utility.ResourceCache;
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
     * : it will usually be more convenient (and more efficient) to call that
     * directly.
     * 
     * @param gvrContext
     *            The GVRF context
     * @param textureCache
     *            Texture cache - may be {@code null}
     * @param callback
     *            Asynchronous notifications
     * @param resource
     *            Stream containing a compressed texture
     * @throws IllegalArgumentException
     *             If {@code gvrContext} or {@code callback} parameters are
     *             {@code null}
     */
    public static void loadCompressedTexture(final GVRContext gvrContext,
            ResourceCache<GVRImage> textureCache,
            final CompressedTextureCallback callback,
            final GVRAndroidResource resource) {
        loadCompressedTexture(gvrContext, textureCache, callback, resource,
                              GVRCompressedImage.DEFAULT_QUALITY);
    }

    /**
     * Load a compressed texture asynchronously.
     * 
     * This is the implementation of
     * {@link GVRContext#loadCompressedTexture(GVRAndroidResource.CompressedTextureCallback, GVRAndroidResource)}
     * : it will usually be more convenient (and more efficient) to call that
     * directly.
     * 
     * @param gvrContext
     *            The GVRF context
     * @param textureCache
     *            Texture cache - may be {@code null}
     * @param callback
     *            Asynchronous notifications
     * @param resource
     *            Basically, a stream containing a compressed texture. Taking a
     *            {@link GVRAndroidResource} parameter eliminates six overloads.
     * @param quality
     *            Speed/quality tradeoff: should be one of
     *            {@link GVRCompressedImage#SPEED},
     *            {@link GVRCompressedImage#BALANCED}, or
     *            {@link GVRCompressedImage#QUALITY}, but other values are
     *            'clamped' to one of the recognized values.
     * @throws IllegalArgumentException
     *             If {@code gvrContext} or {@code callback} parameters are
     *             {@code null}
     */
    public static void loadCompressedTexture(final GVRContext gvrContext,
            final ResourceCache<GVRImage> textureCache,
            final CompressedTextureCallback callback,
            final GVRAndroidResource resource, final int quality)
            throws IllegalArgumentException {
        validateCallbackParameters(gvrContext, callback, resource);

        final GVRImage cached = textureCache == null ? null : textureCache
                .get(resource);
        if (cached != null) {
            Log.v("ASSET", "Texture: %s loaded from cache", cached.getFileName());
            gvrContext.runOnGlThread(new Runnable() {

                @Override
                public void run() {
                    callback.loaded(cached, resource);
                }
            });
        }
        else
        {
            CompressedTextureCallback actualCallback = textureCache == null ? callback
                    : ResourceCache.wrapCallback(textureCache, callback);
            AsyncCompressedTexture.loadTexture(gvrContext,
                                           CancelableCallbackWrapper.wrap(GVRCompressedImage.class, actualCallback),
                                           resource, GVRContext.LOWEST_PRIORITY);
        }
    }

    /**
     * Load a (compressed or bitmapped) texture asynchronously.
     * 
     * This is the implementation of
     * {@link GVRContext#loadTexture(org.gearvrf.GVRAndroidResource.TextureCallback, GVRAndroidResource, int, int)}
     * - it will usually be more convenient (and more efficient) to call that
     * directly.
     * 
     * @param gvrContext
     *            The GVRF context
     * @param textureCache
     *            Texture cache - may be {@code null}
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
        public static void loadTexture(final GVRContext gvrContext,
            final ResourceCache<GVRImage> textureCache,
            final CancelableCallback<GVRImage> callback,
            final GVRAndroidResource resource,
            final int priority,
            final int quality) {
        Threads.spawn(new Runnable() {
            @Override
            public void run() {
                validateCallbackParameters(gvrContext, callback, resource);

                final GVRImage cached = textureCache == null ? null
                        : textureCache.get(resource);
                if (cached != null) {
                    Log.v("ASSET", "Texture: %s loaded from cache", cached.getFileName());
                    callback.loaded(cached, resource);
                } else {
                    // 'Sniff' out compressed textures on a thread from the
                    // thread-pool
                    final GVRCompressedTextureLoader loader = resource
                            .getCompressedLoader();
                    if (loader != null) {
                        CancelableCallback<GVRImage> actualCallback = textureCache == null
                                ? callback
                                : textureCache.wrapCallback(callback);

                        AsyncCompressedTexture.loadTexture(gvrContext,
                                CancelableCallbackWrapper.wrap(
                                        GVRCompressedImage.class,
                                        actualCallback),
                                resource, priority);
                    } else {
                        // We don't have a compressed texture: pass to
                        // AsyncBitmapTexture code
                        CancelableCallback<GVRImage> actualCallback = textureCache == null
                                ? callback
                                : textureCache.wrapCallback(callback);

                        AsyncBitmapTexture.loadTexture(gvrContext,
                                CancelableCallbackWrapper.wrap(
                                        GVRBitmapImage.class, actualCallback),
                                resource, priority);
                    }
                }
            }
        });
    }

    /**
     * Load a (compressed or bitmapped) texture asynchronously.
     * 
     * This is the implementation of
     * {@link GVRAssetLoader#loadTexture(GVRAndroidResource, int, int)} - it
     * will usually be more convenient (and more efficient) to call that
     * directly.
     * 
     * @param gvrContext
     *            The GVRF context
     * @param textureCache
     *            Texture cache - may be {@code null}
     * @param resource
     *            Basically, a stream containing a texture file. The
     *            {@link GVRAndroidResource} class has six constructors to
     *            handle a wide variety of Android resource types. Taking a
     *            {@code GVRAndroidResource} here eliminates six overloads.
     * @param priority
     *            This request's priority. Please see the notes on asynchronous
     *            priorities in the <a href="package-summary.html#async">package
     *            description</a>. Also, please note priorities only apply to
     *            uncompressed textures (standard Android bitmap files, which
     *            can take hundreds of milliseconds to load): compressed
     *            textures load so quickly that they are not run through the
     *            request scheduler.
     * @param quality
     *            The compressed texture {@link GVRCompressedImage#mQuality
     *            quality} parameter: should be one of
     *            {@link GVRCompressedImage#SPEED},
     *            {@link GVRCompressedImage#BALANCED}, or
     *            {@link GVRCompressedImage#QUALITY}, but other values are
     *            'clamped' to one of the recognized values. Please note that
     *            this (currently) only applies to compressed textures; normal
     *            {@linkplain GVRBitmapImage bitmapped textures} don't take a
     *            quality parameter.
     * @return A {@link Future} that you can pass to methods like
     *         {@link GVRShaderData#setMainTexture(Future)}
     */
    public static GVRTexture loadFutureTexture(GVRContext gvrContext,
            ResourceCache<GVRImage> textureCache,
            GVRAndroidResource resource, int priority, int quality) {
        GVRImage cached = textureCache == null ? null : textureCache
                .get(resource);
        GVRTexture tex = new GVRTexture(gvrContext);
        if (cached != null)
        {
            Log.v("ASSET", "Future Texture: %s loaded from cache", cached.getFileName());
            tex.setImage(cached);
        }
        else
        {
            loadTexture(gvrContext, textureCache, tex, resource,
                    priority, quality);
        }
        return tex;
    }

    /**
     * Load a cube map texture asynchronously.
     * 
     * This is the implementation of
     * {@link GVRAssetLoader#loadCubemapTexture(GVRAndroidResource)} - it will
     * usually be more convenient (and more efficient) to call that directly.
     * 
     * @param gvrContext
     *            The GVRF context
     * @param textureCache
     *            Texture cache - may be {@code null}
     * @param resource
     *            A steam containing a zip file which contains six bitmaps. The
     *            six bitmaps correspond to +x, -x, +y, -y, +z, and -z faces of
     *            the cube map texture respectively. The default names of the
     *            six images are "posx.png", "negx.png", "posy.png", "negx.png",
     *            "posz.png", and "negz.png", which can be changed by calling
     *            {@link GVRCubemapImage#setFaceNames(String[])}.
     * @param priority
     *            This request's priority. Please see the notes on asynchronous
     *            priorities in the <a href="package-summary.html#async">package
     *            description</a>.
     * @return A {@link Future} that you can pass to methods like
     *         {@link GVRShaderData#setMainTexture(Future)}
     */
    public static GVRTexture loadFutureCubemapTexture(
            GVRContext gvrContext, ResourceCache<GVRImage> textureCache,
            GVRAndroidResource resource, int priority,
            Map<String, Integer> faceIndexMap) {
        GVRTexture tex = new GVRTexture(gvrContext);
        GVRImage cached = textureCache.get(resource);
        if (cached != null)
        {
            Log.v("ASSET", "Future Texture: %s loaded from cache", cached.getFileName());
            tex.setImage(cached);
        }
        else
        {
            AsyncCubemapTexture.get().loadTexture(gvrContext,
                    CancelableCallbackWrapper.wrap(GVRCubemapImage.class, tex),
                    resource, priority, faceIndexMap);

        }
        return tex;
    }

    /**
     * Load a cubemap texture asynchronously.
     *
     * This is the implementation of
     * {@link GVRAssetLoader#loadCubemapTexture(GVRAndroidResource.TextureCallback, GVRAndroidResource, int)}
     * - it will usually be more convenient (and more efficient) to call that
     * directly.
     *
     * @param context
     *            The GVRF context
     * @param textureCache
     *            Texture cache - may be {@code null}
     * @param callback
     *            Asynchronous notifications
     * @param resource
     *            Basically, a stream containing a compressed texture. Taking a
     *            {@link GVRAndroidResource} parameter eliminates six overloads.
     * @param priority
     *            This request's priority. Please see the notes on asynchronous
     *            priorities in the <a href="package-summary.html#async">package
     *            description</a>.
     * @return A {@link Future} that you can pass to methods like
     *         {@link GVRShaderData#setMainTexture(Future)}
     */
    public static void loadCubemapTexture(final GVRContext context,
                                         ResourceCache<GVRImage> textureCache,
                                         final TextureCallback callback,
                                         final GVRAndroidResource resource, int priority,
                                          Map<String, Integer> faceIndexMap)
            throws IllegalArgumentException
    {
        validatePriorityCallbackParameters(context, callback, resource,
                priority);
        final GVRImage cached = textureCache == null
                ? null
                : (GVRImage) textureCache.get(resource);
        if (cached != null)
        {
            Log.v("ASSET", "Texture: %s loaded from cache", cached.getFileName());
            context.runOnGlThread(new Runnable()
            {
                @Override
                public void run()
                {
                    callback.loaded(cached, resource);
                }
            });
        }
        else
        {
            TextureCallback actualCallback = (textureCache == null) ? callback
                    : ResourceCache.wrapCallback(textureCache, callback);
            AsyncCubemapTexture.loadTexture(context,
                    CancelableCallbackWrapper.wrap(GVRCubemapImage.class, actualCallback),
                    resource, priority, faceIndexMap);
        }
    }

    /**
     * Load a cubemap texture asynchronously.
     *
     * This is the implementation of
     * {@link GVRAssetLoader#loadCompressedCubemapTexture(GVRAndroidResource.TextureCallback, GVRAndroidResource, int)}
     * - it will usually be more convenient (and more efficient) to call that
     * directly.
     *
     * @param context
     *            The GVRF context
     * @param textureCache
     *            Texture cache - may be {@code null}
     * @param callback
     *            Asynchronous notifications
     * @param resource
     *            Basically, a stream containing a compressed texture. Taking a
     *            {@link GVRAndroidResource} parameter eliminates six overloads.
     * @param priority
     *            This request's priority. Please see the notes on asynchronous
     *            priorities in the <a href="package-summary.html#async">package
     *            description</a>.
     */
    public static void loadCompressedCubemapTexture(final GVRContext context,
                                          ResourceCache<GVRImage> textureCache,
                                          final TextureCallback callback,
                                          final GVRAndroidResource resource, int priority,
                                          Map<String, Integer> faceIndexMap)
            throws IllegalArgumentException
    {
        validatePriorityCallbackParameters(context, callback, resource,
                                           priority);
        final GVRImage cached = textureCache == null
                ? null
                : (GVRImage) textureCache.get(resource);
        if (cached != null)
        {
            Log.v("ASSET", "Texture: %s loaded from cache", cached.getFileName());
            context.runOnGlThread(new Runnable()
            {
                @Override
                public void run()
                {
                    callback.loaded(cached, resource);
                }
            });
        }
        else
        {
            TextureCallback actualCallback = (textureCache == null) ? callback
                    : ResourceCache.wrapCallback(textureCache, callback);
            AsyncCompressedCubemapTexture.loadTexture(context, CancelableCallbackWrapper.wrap(GVRCompressedCubemapImage.class, actualCallback),
                                            resource, priority, faceIndexMap);
        }
    }

    /**
     * 
     * Load a GL mesh asynchronously.
     * 
     * This is the implementation of
     * {@link GVRAssetLoader#loadMesh(GVRAndroidResource.MeshCallback, GVRAndroidResource, int)}
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
    // This method does not take a ResourceCache<GVRMeh> parameter because it
    // (indirectly) calls GVRContext.loadMesh() which 'knows about' the cache
    public static void loadMesh(GVRContext gvrContext,
            CancelableCallback<GVRMesh> callback, GVRAndroidResource resource,
            int priority) {
        validatePriorityCallbackParameters(gvrContext, callback, resource,
                priority);

        AsyncMesh.get().loadMesh(gvrContext, callback, resource, priority);
    }

    public static class FutureResource<T extends GVRHybridObject> implements
            Future<T> {

        private static final String TAG = Log.tag(FutureResource.class);

        /** Do all our synchronization on data private to this instance */
        private final Object[] lock = new Object[0];

        private T result = null;
        private Throwable error = null;
        private boolean pending = true;
        private boolean canceled = false;

        private GVRAndroidResource resource;

        private final CancelableCallback<T> callback = new CancelableCallback<T>() {

            @Override
            public void loaded(T data, GVRAndroidResource androidResource) {
                synchronized (lock) {
                    result = data;
                    pending = false;
                    lock.notifyAll();
                }
            }

            @Override
            public void failed(Throwable t, GVRAndroidResource androidResource) {
                Log.d(TAG, "failed(%s), %s", androidResource, t);
                synchronized (lock) {
                    error = t;
                    pending = false;
                    lock.notifyAll();
                }
            }

            @Override
            public boolean stillWanted(GVRAndroidResource androidResource) {
                return canceled == false;
            }
        };

        public FutureResource(GVRAndroidResource resource) {
            this.resource = resource;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            canceled = true;
            return pending;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return get(0);
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException,
                ExecutionException, TimeoutException {
            return get(unit.toMillis(timeout));
        }

        private T get(long millis)
                throws InterruptedException, ExecutionException {
            if (!pending) {
                return result;
            }

            synchronized (lock) {
                if (pending) {                    
                    lock.wait(millis);
                }
            }

            if (canceled) {
                throw new CancellationException();
            }
            if (error != null) {
                throw new ExecutionException(error);
            }
            return result;
        }

        @Override
        public boolean isCancelled() {
            return canceled;
        }

        @Override
        public boolean isDone() {
            return pending == false;
        }

        public GVRAndroidResource getResource() {
            return resource;
        }
    }

    /*
     * This is a wrapper to convert {@code CancelableCallback<S>} to {@code CancelableCallback<T>}
     * where T extends S.
     */
    static class CancelableCallbackWrapper<S extends GVRHybridObject, T extends S>
    implements CancelableCallback<T> {
        private CancelableCallback<S> wrapped_;

        private CancelableCallbackWrapper(CancelableCallback<S> wrapped) {
            wrapped_ = wrapped;
        }

        @Override
        public void loaded(T resource, GVRAndroidResource androidResource) {
            wrapped_.loaded(resource, androidResource);
        }

        @Override
        public void failed(Throwable t, GVRAndroidResource androidResource) {
            wrapped_.failed(t, androidResource);
        }

        @Override
        public boolean stillWanted(GVRAndroidResource androidResource) {
            return wrapped_.stillWanted(androidResource);
        }

        public static <S extends GVRHybridObject, T extends S> CancelableCallbackWrapper<S, T> wrap(
                Class<T> targetClass,
                CancelableCallback<S> wrapped) {
            return new CancelableCallbackWrapper<S, T>(wrapped);
        }
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
     * A synchronous (blocking) wrapper around
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

    /**
     * Load a atlas map information asynchronously.
     *
     * @param ins
     *            JSON text stream
     */
    public static List<GVRAtlasInformation> loadAtlasInformation(InputStream ins) {
        return AsyncAtlasInfo.loadAtlasInformation(ins);
    }
}
