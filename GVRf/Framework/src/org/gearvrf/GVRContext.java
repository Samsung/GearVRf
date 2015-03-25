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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.gearvrf.GVRAndroidResource.BitmapTextureCallback;
import org.gearvrf.GVRAndroidResource.CompressedTextureCallback;
import org.gearvrf.GVRAndroidResource.MeshCallback;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.asynchronous.GVRAsynchronousResourceLoader;
import org.gearvrf.asynchronous.GVRCompressedTexture;
import org.gearvrf.asynchronous.GVRCompressedTextureLoader;
import org.gearvrf.periodic.GVRPeriodicEngine;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.KeyEvent;

/**
 * Like the Android {@link Context} class, {@code GVRContext} provides core
 * services, and global information about an application environment.
 * 
 * Use {@code GVRContext} to {@linkplain #createQuad(float, float) create} and
 * {@linkplain #loadMesh(String) load} GL meshes, Android
 * {@linkplain #loadBitmap(String) bitmaps}, and
 * {@linkplain #loadTexture(String) GL textures.} {@code GVRContext} also holds
 * the {@linkplain GVRScene main scene} and miscellaneous information like
 * {@linkplain #getFrameTime() the frame time.}
 */
public abstract class GVRContext {
    private final Context mContext;

    /*
     * Fields and constants
     */

    // Priorities constants, for asynchronous loading

    /**
     * GVRF can't use every {@code int} as a priority - it needs some sentinel
     * values. It will probably never need anywhere near this many, but raising
     * the number of reserved values narrows the 'dynamic range' available to
     * apps mapping some internal score to the {@link #LOWEST_PRIORITY} to
     * {@link #HIGHEST_PRIORITY} range, and might change app behavior in subtle
     * ways that seem best avoided.
     * 
     * @since 1.6.1
     */
    public static final int RESERVED_PRIORITIES = 1024;

    /**
     * GVRF can't use every {@code int} as a priority - it needs some sentinel
     * values. A simple approach to generating priorities is to score resources
     * from 0 to 1, and then map that to the range {@link #LOWEST_PRIORITY} to
     * {@link #HIGHEST_PRIORITY}.
     * 
     * @since 1.6.1
     */
    public static final int LOWEST_PRIORITY = Integer.MIN_VALUE
            + RESERVED_PRIORITIES;

    /**
     * GVRF can't use every {@code int} as a priority - it needs some sentinel
     * values. A simple approach to generating priorities is to score resources
     * from 0 to 1, and then map that to the range {@link #LOWEST_PRIORITY} to
     * {@link #HIGHEST_PRIORITY}.
     * 
     * @since 1.6.1
     */
    public static final int HIGHEST_PRIORITY = Integer.MAX_VALUE;

    /**
     * The priority used by
     * {@link #loadBitmapTexture(GVRAndroidResource.BitmapTextureCallback, GVRAndroidResource)}
     * and
     * {@link #loadMesh(GVRAndroidResource.MeshCallback, GVRAndroidResource)}
     * 
     * @since 1.6.1
     */
    public static final int DEFAULT_PRIORITY = 0;

    /*
     * Methods
     */

    GVRContext(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Get the Android {@link Context}, which provides access to system services
     * and to your application's resources. This is <em>not</em> your
     * {@link GVRActivity} implementation, but rather the
     * {@linkplain Activity#getApplicationContext() application context,} which
     * is usually an {@link Application}.
     * 
     * @return An Android {@code Context}
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * Loads a file placed in the assets directory (or one of its
     * subdirectories) as a {@link GVRMesh}.
     * 
     * Contrast this overload with {@link #loadMesh(int)}, which can load a
     * {@code res/raw} file. See the discussion of asset-relative filenames
     * <i>vs.</i> {@code R.raw} resource ids in the <a
     * href="package-summary.html#assets">package description</a>.
     * 
     * @param fileName
     *            The name of a file, relative to the assets directory. The
     *            assets directory may contain an arbitrarily complex tree of
     *            subdirectories; the file name can specify any location in or
     *            under the assets directory.
     * @return The file as a GL mesh or {@code null} if the file does not exist
     *         (or cannot be read)
     * 
     * @deprecated We will continue to support the overload jungle (at least
     *             until public release) but suggest that you switch to the new,
     *             simpler {@link #loadMesh(GVRAndroidResource)}
     */

    public GVRMesh loadMesh(String fileName) {
        GVRAssimpImporter assimpImporter = GVRImporter.readFileFromAssets(this,
                fileName);
        return assimpImporter == null ? null : assimpImporter.getMesh(0);
    }

    /**
     * Loads a file (placed anywhere on the file system) as a {@link GVRMesh}.
     * 
     * {@link #loadMesh(int)} and {@link #loadMesh(String)} are your best choice
     * for loading resources compiled into your app; this method is best for
     * loading downloaded (or dynamically generated) content stored somewhere in
     * your app's private storage.
     * 
     * @param file
     *            Path to the file
     * @return The file as a GL mesh or {@code null} if the file does not exist
     *         (or cannot be read)
     * 
     * @deprecated We will continue to support the overload jungle (at least
     *             until public release) but suggest that you switch to the new,
     *             simpler {@link #loadMesh(GVRAndroidResource)}
     */
    public GVRMesh loadMesh(File file) {
        if (file.canRead() != true) {
            return null;
        }
        String absolutePath = file.getAbsolutePath();
        GVRAssimpImporter assimpImporter = GVRImporter.readFileFromSDCard(this,
                absolutePath);
        return assimpImporter.getMesh(0);
    }

    /**
     * 100% synonymous with {@link #loadMesh(String)}.
     * 
     * Introduced solely to maintain parallelism with
     * {@link #loadMeshFromFileSystem(String)}. Contrast this method with
     * {@link #loadMesh(int)}, which can load a {@code res/raw} file. See the
     * discussion of asset-relative filenames <i>vs.</i> {@code R.raw} resource
     * ids in the <a href="package-summary.html#assets">package description</a>.
     * 
     * @param assetRelativeFileName
     *            The name of a file, relative to the assets directory. The
     *            assets directory may contain an arbitrarily complex tree of
     *            subdirectories; the file name can specify any location in or
     *            under the assets directory.
     * @return The file as a GL mesh or {@code null} if the file does not exist
     *         (or cannot be read)
     * 
     * @deprecated We will continue to support the overload jungle (at least
     *             until public release) but suggest that you switch to the new,
     *             simpler {@link #loadMesh(GVRAndroidResource)}
     */
    public GVRMesh loadMeshFromAssets(String assetRelativeFileName) {
        return loadMesh(assetRelativeFileName);
    }

    /**
     * Convenience function that wraps {@link #loadMesh(File)}, constructing the
     * {@link File} object for you.
     * 
     * {@link #loadMesh(int)} and {@link #loadMesh(String)} are your best choice
     * for loading resources compiled into your app; this method is best for
     * loading downloaded (or dynamically generated) content stored somewhere in
     * your app's private storage.
     * 
     * @param fileName
     *            Path to the file
     * @return The file as a GL mesh or {@code null} if the file does not exist
     *         (or cannot be read)
     * 
     * @deprecated We will continue to support the overload jungle (at least
     *             until public release) but suggest that you switch to the new,
     *             simpler {@link #loadMesh(GVRAndroidResource)}
     */
    public GVRMesh loadMeshFromFileSystem(String fileName) {
        return loadMesh(new File(fileName));
    }

    /**
     * Loads a {@code res/raw} file {@link GVRMesh}.
     * 
     * Contrast this overload with {@link #loadMesh(String)}, which can load a
     * file from the {@code assets} directory. See the discussion of
     * asset-relative filenames <i>vs.</i> {@code R.raw} resource ids in the <a
     * href="package-summary.html#assets">package description</a>.
     * 
     * @param resourceId
     *            The Android-generated id of a file in your {@code res/raw}
     *            directory.
     * @return The file as a GL mesh.
     * 
     * @deprecated We will continue to support the overload jungle (at least
     *             until public release) but suggest that you switch to the new,
     *             simpler {@link #loadMesh(GVRAndroidResource)}
     */

    public GVRMesh loadMesh(int resourceId) {
        GVRAssimpImporter assimpImporter = GVRImporter.readFileFromResources(
                this, resourceId);
        return assimpImporter.getMesh(0);
    }

    /**
     * Loads a file as a {@link GVRMesh}.
     * 
     * Note that this method can be quite slow; we recommend never calling it
     * from the GL thread. The asynchronous version
     * {@link #loadMesh(GVRAndroidResource.MeshCallback, GVRAndroidResource)} is
     * better because it moves most of the work to a background thread, doing as
     * little as possible on the GL thread.
     * 
     * @param androidResource
     *            Basically, a stream containing a 3D model. The
     *            {@link GVRAndroidResource} class has six constructors to
     *            handle a wide variety of Android resource types. Taking a
     *            {@code GVRAndroidResource} here eliminates six overloads.
     * @return The file as a GL mesh.
     * 
     * @since 1.6.2
     */
    public GVRMesh loadMesh(GVRAndroidResource androidResource) {
        GVRAssimpImporter assimpImporter = GVRImporter.readFileFromResources(
                this, androidResource);
        return assimpImporter.getMesh(0);
    }

    /**
     * Loads a mesh file, asynchronously, at a default priority.
     * 
     * This method and the
     * {@linkplain #loadMesh(GVRAndroidResource.MeshCallback, GVRAndroidResource, int)
     * overload that takes a priority} are generally going to be your best
     * choices for loading {@link GVRMesh} resources: mesh loading can take
     * hundreds - and even thousands - of milliseconds, and so should not be
     * done on the GL thread in either {@link GVRScript#onInit(GVRContext)
     * onInit()} or {@link GVRScript#onStep() onStep()}.
     * 
     * <p>
     * The asynchronous methods improve throughput in three ways. First, by
     * doing all the work on a background thread, then delivering the loaded
     * mesh to the GL thread on a {@link #runOnGlThread(Runnable)
     * runOnGlThread()} callback. Second, they use a throttler to avoid
     * overloading the system and/or running out of memory. Third, they do
     * 'request consolidation' - if you issue any requests for a particular file
     * while there is still a pending request, the file will only be read once,
     * and each callback will get the same {@link GVRMesh}.
     * 
     * @param callback
     *            App supplied callback, with three different methods.
     *            <ul>
     *            <li>Before loading, GVRF may call
     *            {@link GVRAndroidResource.MeshCallback#stillWanted(GVRAndroidResource)
     *            stillWanted()} (on a background thread) to give you a chance
     *            to abort a 'stale' load.
     * 
     *            <li>Successful loads will call
     *            {@link GVRAndroidResource.MeshCallback#loaded(GVRMesh, GVRAndroidResource)
     *            loaded()} on the GL thread.
     * 
     *            <li>Any errors will call
     *            {@link GVRAndroidResource.MeshCallback#failed(Throwable, GVRAndroidResource)
     *            failed(),} with no promises about threading.
     *            </ul>
     * @param androidResource
     *            Basically, a stream containing a 3D model. The
     *            {@link GVRAndroidResource} class has six constructors to
     *            handle a wide variety of Android resource types. Taking a
     *            {@code GVRAndroidResource} here eliminates six overloads.
     * 
     * @throws IllegalArgumentException
     *             If either parameter is {@code null}
     * 
     * @since 1.6.2
     */
    public void loadMesh(MeshCallback callback,
            GVRAndroidResource androidResource) throws IllegalArgumentException {
        loadMesh(callback, androidResource, DEFAULT_PRIORITY);
    }

    /**
     * Loads a mesh file, asynchronously, at an explicit priority.
     * 
     * This method and the
     * {@linkplain #loadMesh(GVRAndroidResource.MeshCallback, GVRAndroidResource)
     * overload that supplies a default priority} are generally going to be your
     * best choices for loading {@link GVRMesh} resources: mesh loading can take
     * hundreds - and even thousands - of milliseconds, and so should not be
     * done on the GL thread in either {@link GVRScript#onInit(GVRContext)
     * onInit()} or {@link GVRScript#onStep() onStep()}.
     * 
     * <p>
     * The asynchronous methods improve throughput in three ways. First, by
     * doing all the work on a background thread, then delivering the loaded
     * mesh to the GL thread on a {@link #runOnGlThread(Runnable)
     * runOnGlThread()} callback. Second, they use a throttler to avoid
     * overloading the system and/or running out of memory. Third, they do
     * 'request consolidation' - if you issue any requests for a particular file
     * while there is still a pending request, the file will only be read once,
     * and each callback will get the same {@link GVRMesh}.
     * 
     * @param callback
     *            App supplied callback, with three different methods.
     *            <ul>
     *            <li>Before loading, GVRF may call
     *            {@link GVRAndroidResource.MeshCallback#stillWanted(GVRAndroidResource)
     *            stillWanted()} (on a background thread) to give you a chance
     *            to abort a 'stale' load.
     * 
     *            <li>Successful loads will call
     *            {@link GVRAndroidResource.MeshCallback#loaded(GVRMesh, GVRAndroidResource)
     *            loaded()} on the GL thread.
     * 
     *            <li>Any errors will call
     *            {@link GVRAndroidResource.MeshCallback#failed(Throwable, GVRAndroidResource)
     *            failed(),} with no promises about threading.
     *            </ul>
     * @param androidResource
     *            Basically, a stream containing a 3D model. The
     *            {@link GVRAndroidResource} class has six constructors to
     *            handle a wide variety of Android resource types. Taking a
     *            {@code GVRAndroidResource} here eliminates six overloads.
     * @param priority
     *            This request's priority. Please see the notes on asynchronous
     *            priorities in the <a href="package-summary.html#async">package
     *            description</a>.
     * 
     * @throws IllegalArgumentException
     *             If either {@code callback} or {@code resource} is
     *             {@code null}, or if {@code priority} is out of range.
     * 
     * @since 1.6.2
     */
    public void loadMesh(MeshCallback callback, GVRAndroidResource resource,
            int priority) throws IllegalArgumentException {
        GVRAsynchronousResourceLoader.loadMesh(this, callback, resource,
                priority);
    }

    /**
     * Creates a quad consisting of two triangles, with the specified width and
     * height.
     * 
     * @param width
     *            the quad's width
     * @param height
     *            the quad's height
     * @return A 2D, rectangular mesh with four vertices and two triangles
     */
    public GVRMesh createQuad(float width, float height) {
        GVRMesh mesh = new GVRMesh(this);

        float[] vertices = { width * -0.5f, height * 0.5f, 0.0f, width * -0.5f,
                height * -0.5f, 0.0f, width * 0.5f, height * 0.5f, 0.0f,
                width * 0.5f, height * -0.5f, 0.0f };
        mesh.setVertices(vertices);

        float[] texCoords = { 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f };
        mesh.setTexCoords(texCoords);

        char[] triangles = { 0, 1, 2, 1, 3, 2 };
        mesh.setTriangles(triangles);

        return mesh;
    }

    /**
     * Loads file placed in the assets folder, as a {@link Bitmap}.
     * 
     * <p>
     * Note that this method may take hundreds of milliseconds to return: unless
     * the bitmap is quite tiny, you probably don't want to call this directly
     * from your {@link GVRScript#onStep() onStep()} callback as that is called
     * once per frame, and a long call will cause you to miss frames.
     * 
     * <p>
     * Note also that this method does no scaling, and will return a full-size
     * {@link Bitmap}. Loading (say) an unscaled photograph may abort your app:
     * Use pre-scaled images, or {@link BitmapFactory} methods which give you
     * more control over the image size.
     * 
     * @param fileName
     *            The name of a file, relative to the assets directory. The
     *            assets directory may contain an arbitrarily complex tree of
     *            subdirectories; the file name can specify any location in or
     *            under the assets directory.
     * @return The file as a bitmap, or {@code null} if file path does not exist
     *         or the file can not be decoded into a Bitmap.
     */
    public Bitmap loadBitmap(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("File name should not be null.");
        }
        InputStream stream = null;
        Bitmap bitmap = null;
        try {
            try {
                stream = mContext.getAssets().open(fileName);
                return bitmap = BitmapFactory.decodeStream(stream);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Don't discard a valid Bitmap because of an IO error closing the
            // file!
            return bitmap;
        }
    }

    /**
     * Loads file placed in the assets folder, as a {@link GVRBitmapTexture}.
     * 
     * <p>
     * Note that this method may take hundreds of milliseconds to return: unless
     * the texture is quite tiny, you probably don't want to call this directly
     * from your {@link GVRScript#onStep() onStep()} callback as that is called
     * once per frame, and a long call will cause you to miss frames. For large
     * images, you should use either
     * {@link #loadBitmapTexture(GVRAndroidResource.BitmapTextureCallback, GVRAndroidResource)
     * loadBitmapTexture()} (faster) or
     * {@link #loadCompressedTexture(GVRAndroidResource.CompressedTextureCallback, GVRAndroidResource)}
     * (fastest <em>and</em> least memory pressure).
     * 
     * <p>
     * Note also that this method does no scaling, and will return a full-size
     * {@link Bitmap}. Loading (say) an unscaled photograph may abort your app:
     * Use
     * <ul>
     * <li>Pre-scaled images
     * <li>{@link BitmapFactory} methods which give you more control over the
     * image size, or
     * <li>
     * {@link #loadBitmapTexture(GVRAndroidResource.BitmapTextureCallback, GVRAndroidResource)}
     * which automatically scales large images to fit the GPU's restrictions and
     * to avoid {@linkplain OutOfMemoryError out of mremory errors.}
     * </ul>
     * 
     * @param fileName
     *            The name of a file, relative to the assets directory. The
     *            assets directory may contain an arbitrarily complex tree of
     *            subdirectories; the file name can specify any location in or
     *            under the assets directory.
     * @return The file as a texture, or {@code null} if file path does not
     *         exist or the file can not be decoded into a Bitmap.
     */
    public GVRBitmapTexture loadTexture(String fileName) {
        if (fileName.endsWith(".png")) { // load png directly to texture
            return new GVRBitmapTexture(this, fileName);
        }

        Bitmap bitmap = loadBitmap(fileName);
        return bitmap == null ? null : new GVRBitmapTexture(this, bitmap);
    }

    /**
     * Load a bitmap, asynchronously, with a default priority.
     * 
     * Because it is asynchronous, this method <em>is</em> a bit harder to use
     * than {@link #loadTexture(String)}, but it moves a large amount of work
     * (in {@link BitmapFactory#decodeStream(InputStream)} from the GL thread to
     * a background thread. Since you <em>can</em> create a
     * {@link GVRSceneObject} without a mesh and texture - and set them later -
     * using the asynchronous API can improve startup speed and/or reduce frame
     * misses (where an {@link GVRScript#onStep() onStep()} takes too long).
     * This API may also use less RAM than {@link #loadTexture(String)}.
     * 
     * <p>
     * This API will 'consolidate' requests: If you request a texture like
     * {@code R.raw.wood_grain} and then - before it has loaded - issue another
     * request for {@code R.raw.wood_grain}, GVRF will only read the bitmap file
     * once; only create a single {@link GVRTexture}; and then call both
     * callbacks, passing each the same texture.
     * 
     * <p>
     * Please be aware that {@link BitmapFactory#decodeStream(InputStream)} is a
     * comparatively expensive operation: it can take hundreds of milliseconds
     * and use several megabytes of temporary RAM. GVRF includes a throttler to
     * keep the total load manageable - but
     * {@link #loadCompressedTexture(GVRAndroidResource.CompressedTextureCallback, GVRAndroidResource)}
     * is <em>much</em> faster and lighter-weight: that API simply loads the
     * compressed texture into a small amount RAM (which doesn't take very long)
     * and does some simple parsing to figure out the parameters to pass
     * {@code glCompressedTexImage2D()}. The GL hardware does the decoding much
     * faster than Android's {@link BitmapFactory}!
     * 
     * <p>
     * TODO Take a boolean parameter that controls mipmap generation?
     * 
     * @since 1.6.1
     * 
     * @param callback
     *            Before loading, GVRF may call
     *            {@link GVRAndroidResource.BitmapTextureCallback#stillWanted(GVRAndroidResource)
     *            stillWanted()} several times (on a background thread) to give
     *            you a chance to abort a 'stale' load.
     * 
     *            Successful loads will call
     *            {@link GVRAndroidResource.BitmapTextureCallback#loaded(GVRTexture, GVRAndroidResource)
     *            loaded()} on the GL thread;
     * 
     *            any errors will call
     *            {@link GVRAndroidResource.BitmapTextureCallback#failed(Throwable, GVRAndroidResource)
     *            failed()}, with no promises about threading.
     * 
     *            <p>
     *            This method uses a throttler to avoid overloading the system.
     *            If the throttler has threads available, it will run this
     *            request immediately. Otherwise, it will enqueue the request,
     *            and call
     *            {@link GVRAndroidResource.BitmapTextureCallback#stillWanted(GVRAndroidResource)
     *            stillWanted()} at least once (on a background thread) to give
     *            you a chance to abort a 'stale' load.
     * @param resource
     *            Basically, a stream containing a bitmapped image. The
     *            {@link GVRAndroidResource} class has six constructors to
     *            handle a wide variety of Android resource types. Taking a
     *            {@code GVRAndroidResource} here eliminates six overloads.
     */
    public void loadBitmapTexture(BitmapTextureCallback callback,
            GVRAndroidResource resource) {
        loadBitmapTexture(callback, resource, DEFAULT_PRIORITY);
    }

    /**
     * Load a bitmap, asynchronously, with an explicit priority.
     * 
     * Because it is asynchronous, this method <em>is</em> a bit harder to use
     * than {@link #loadTexture(String)}, but it moves a large amount of work
     * (in {@link BitmapFactory#decodeStream(InputStream)} from the GL thread to
     * a background thread. Since you <em>can</em> create a
     * {@link GVRSceneObject} without a mesh and texture - and set them later -
     * using the asynchronous API can improve startup speed and/or reduce frame
     * misses, where an {@link GVRScript#onStep() onStep()} takes too long.
     * 
     * <p>
     * This API will 'consolidate' requests: If you request a texture like
     * {@code R.raw.wood_grain} and then - before it has loaded - issue another
     * request for {@code R.raw.wood_grain}, GVRF will only read the bitmap file
     * once; only create a single {@link GVRTexture}; and then call both
     * callbacks, passing each the same texture.
     * 
     * <p>
     * Please be aware that {@link BitmapFactory#decodeStream(InputStream)} is a
     * comparatively expensive operation: it can take hundreds of milliseconds
     * and use several megabytes of temporary RAM. GVRF includes a throttler to
     * keep the total load manageable - but
     * {@link #loadCompressedTexture(GVRAndroidResource.CompressedTextureCallback, GVRAndroidResource)}
     * is <em>much</em> faster and lighter-weight: that API simply loads the
     * compressed texture into a small amount RAM (which doesn't take very long)
     * and does some simple parsing to figure out the parameters to pass
     * {@code glCompressedTexImage2D()}. The GL hardware does the decoding much
     * faster than Android's {@link BitmapFactory}!
     * 
     * @since 1.6.1
     * 
     * @param callback
     *            Before loading, GVRF may call
     *            {@link GVRAndroidResource.BitmapTextureCallback#stillWanted(GVRAndroidResource)
     *            stillWanted()} several times (on a background thread) to give
     *            you a chance to abort a 'stale' load.
     * 
     *            Successful loads will call
     *            {@link GVRAndroidResource.BitmapTextureCallback#loaded(GVRTexture, GVRAndroidResource)
     *            loaded()} on the GL thread;
     * 
     *            any errors will call
     *            {@link GVRAndroidResource.BitmapTextureCallback#failed(Throwable, GVRAndroidResource)
     *            failed()}, with no promises about threading.
     * 
     *            <p>
     *            This method uses a throttler to avoid overloading the system.
     *            If the throttler has threads available, it will run this
     *            request immediately. Otherwise, it will enqueue the request,
     *            and call
     *            {@link GVRAndroidResource.BitmapTextureCallback#stillWanted(GVRAndroidResource)
     *            stillWanted()} at least once (on a background thread) to give
     *            you a chance to abort a 'stale' load.
     * @param resource
     *            Basically, a stream containing a bitmapped image. The
     *            {@link GVRAndroidResource} class has six constructors to
     *            handle a wide variety of Android resource types. Taking a
     *            {@code GVRAndroidResource} here eliminates six overloads.
     * @param priority
     *            This request's priority. Please see the notes on asynchronous
     *            priorities in the <a href="package-summary.html#async">package
     *            description</a>.
     * 
     * @throws IllegalArgumentException
     *             If {@code priority} {@literal <} {@link #LOWEST_PRIORITY} or
     *             {@literal >} {@link #HIGHEST_PRIORITY}, or either of the
     *             other parameters is {@code null}
     */
    public void loadBitmapTexture(BitmapTextureCallback callback,
            GVRAndroidResource resource, int priority)
            throws IllegalArgumentException {
        GVRAsynchronousResourceLoader.loadBitmapTexture(this, callback,
                resource, priority);
    }

    /**
     * Load a compressed texture, asynchronously.
     * 
     * GVRF currently supports ASTC, ETC2, and KTX formats: applications can add
     * new formats by implementing {@link GVRCompressedTextureLoader}.
     * 
     * <p>
     * This method uses the fastest possible rendering. To specify higher
     * quality (but slower) rendering, you can use the
     * {@link #loadCompressedTexture(GVRAndroidResource.CompressedTextureCallback, GVRAndroidResource, int)}
     * overload.
     * 
     * @since 1.6.1
     * 
     * @param callback
     *            Successful loads will call
     *            {@link GVRAndroidResource.CompressedTextureCallback#loaded(GVRTexture, GVRAndroidResource)
     *            loaded()} on the GL thread; any errors will call
     *            {@link GVRAndroidResource.CompressedTextureCallback#failed(Throwable, GVRAndroidResource)
     *            failed()}, with no promises about threading.
     * @param resource
     *            Basically, a stream containing a compressed texture. The
     *            {@link GVRAndroidResource} class has six constructors to
     *            handle a wide variety of Android resource types. Taking a
     *            {@code GVRAndroidResource} here eliminates six overloads.
     */
    public void loadCompressedTexture(CompressedTextureCallback callback,
            GVRAndroidResource resource) {
        GVRAsynchronousResourceLoader.loadCompressedTexture(this, callback,
                resource);
    }

    /**
     * Load a compressed texture, asynchronously.
     * 
     * GVRF currently supports ASTC, ETC2, and KTX formats: applications can add
     * new formats by implementing {@link GVRCompressedTextureLoader}.
     * 
     * @since 1.6.1
     * 
     * @param callback
     *            Successful loads will call
     *            {@link GVRAndroidResource.CompressedTextureCallback#loaded(GVRTexture, GVRAndroidResource)}
     *            on the GL thread; any errors will call
     *            {@link GVRAndroidResource.CompressedTextureCallback#failed(Throwable, GVRAndroidResource)}
     *            , with no promises about threading.
     * @param resource
     *            Basically, a stream containing a compressed texture. The
     *            {@link GVRAndroidResource} class has six constructors to
     *            handle a wide variety of Android resource types. Taking a
     *            {@code GVRAndroidResource} here eliminates six overloads.
     * @param quality
     *            Speed/quality tradeoff: should be one of
     *            {@link GVRCompressedTexture#SPEED},
     *            {@link GVRCompressedTexture#BALANCED}, or
     *            {@link GVRCompressedTexture#QUALITY}, but other values are
     *            'clamped' to one of the recognized values.
     */
    public void loadCompressedTexture(CompressedTextureCallback callback,
            GVRAndroidResource resource, int quality) {
        GVRAsynchronousResourceLoader.loadCompressedTexture(this, callback,
                resource, quality);
    }

    /**
     * Get the current {@link GVRScene}, which contains the scene graph (a
     * hierarchy of {@linkplain GVRSceneObject scene objects}) and the
     * {@linkplain GVRCameraRig camera rig}
     * 
     * @return A {@link GVRScene} instance, containing scene and camera
     *         information
     */
    public abstract GVRScene getMainScene();

    /** Set the current {@link GVRScene} */
    public abstract void setMainScene(GVRScene scene);

    /**
     * Is the key pressed?
     * 
     * @param keyCode
     *            An Android {@linkplain KeyEvent#KEYCODE_0 key code}
     */
    public abstract boolean isKeyDown(int keyCode);

    /**
     * The interval between this frame and the previous frame, in seconds: a
     * rough gauge of Frames Per Second.
     */
    public abstract float getFrameTime();

    /**
     * Enqueues a callback to be run in the GL thread.
     * 
     * This is how you take data generated on a background thread (or the main
     * (GUI) thread) and pass it to the coprocessor, using calls that must be
     * made from the GL thread (aka the "GL context"). The callback queue is
     * processed before any registered
     * {@linkplain #registerDrawFrameListener(GVRDrawFrameListener) frame
     * listeners}.
     * 
     * @param runnable
     *            A bit of code that must run on the GL thread
     */
    public abstract void runOnGlThread(Runnable runnable);

    /**
     * Subscribes a {@link GVRDrawFrameListener}.
     * 
     * Each frame listener is called, once per frame, after any pending
     * {@linkplain #runOnGlThread(Runnable) GL callbacks} and before
     * {@link GVRScript#onStep()}.
     * 
     * @param frameListener
     *            A callback that will fire once per frame, until it is
     *            {@linkplain #unregisterDrawFrameListener(GVRDrawFrameListener)
     *            unregistered}
     */
    public abstract void registerDrawFrameListener(
            GVRDrawFrameListener frameListener);

    /**
     * Remove a previously-subscribed {@link GVRDrawFrameListener}.
     * 
     * @param frameListener
     *            An instance of a {@link GVRDrawFrameListener} implementation.
     *            Unsubscribing a listener which is not actually subscribed will
     *            not throw an exception.
     */
    public abstract void unregisterDrawFrameListener(
            GVRDrawFrameListener frameListener);

    /**
     * The {@linkplain GVRMaterialShaderManager object shader manager}
     * singleton.
     * 
     * Use the shader manager to define custom GL object shaders, which are used
     * to render a scene object's surface.
     * 
     * @return The {@linkplain GVRMaterialShaderManager shader manager}
     *         singleton.
     */
    public GVRMaterialShaderManager getMaterialShaderManager() {
        return getRenderBundle().getMaterialShaderManager();
    }

    /**
     * The {@linkplain GVRMaterialShaderManager object shader manager}
     * singleton.
     * 
     * Use the shader manager to define custom GL object shaders, which are used
     * to render a scene object's surface.
     * 
     * @return The {@linkplain GVRMaterialShaderManager shader manager}
     *         singleton.
     * 
     * @deprecated Please use {@link #getMaterialShaderManager()}
     */
    public GVRMaterialShaderManager getShaderManager() {
        return getMaterialShaderManager();
    }

    /**
     * The {@linkplain GVRPostEffectShaderManager scene shader manager}
     * singleton.
     * 
     * Use the shader manager to define custom GL scene shaders, which can be
     * inserted into the rendering pipeline to apply image processing effects to
     * the rendered scene graph. In classic GL programming, this is often
     * referred to as a "post effect."
     * 
     * @return The {@linkplain GVRPostEffectShaderManager post effect shader
     *         manager} singleton.
     */
    public GVRPostEffectShaderManager getPostEffectShaderManager() {
        return getRenderBundle().getPostEffectShaderManager();
    }

    /**
     * The {@linkplain GVRAnimationEngine animation engine} singleton.
     * 
     * Use the animation engine to start and stop {@linkplain GVRAnimation
     * animations}.
     * 
     * @return The {@linkplain GVRAnimationEngine animation engine} singleton.
     */
    public GVRAnimationEngine getAnimationEngine() {
        return GVRAnimationEngine.getInstance(this);
    }

    /**
     * The {@linkplain GVRPeriodicEngine periodic engine} singleton.
     * 
     * Use the periodic engine to schedule {@linkplain Runnable runnables} to
     * run on the GL thread at a future time.
     * 
     * @return The {@linkplain GVRPeriodicEngine periodic engine} singleton.
     */
    public GVRPeriodicEngine getPeriodicEngine() {
        return GVRPeriodicEngine.getInstance(this);
    }

    abstract GVRReferenceQueue getReferenceQueue();

    abstract GVRRenderBundle getRenderBundle();

    abstract GVRRecyclableObjectProtector getRecyclableObjectProtector();
}
