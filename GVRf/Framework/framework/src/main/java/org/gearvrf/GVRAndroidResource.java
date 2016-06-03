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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.TypedValue;

import org.gearvrf.asynchronous.CompressedTexture;
import org.gearvrf.asynchronous.GVRCompressedTextureLoader;
import org.gearvrf.utility.Log;
import org.gearvrf.utility.MarkingFileInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A class to minimize overload fan-out.
 * 
 * APIs that load resources can take a {@link GVRAndroidResource} instead of
 * having overloads for {@code assets} files, {@code res/drawable} and
 * {@code res/raw} files, and plain old files.
 * 
 * See the discussion of asset-relative filenames <i>vs.</i> {@code R.raw}
 * resource ids in the <a href="package-summary.html#assets">package
 * description</a>.
 * 
 * @since 1.6.1
 */
public class GVRAndroidResource {
    private static final String TAG = Log.tag(GVRAndroidResource.class);

    private enum StreamStates {
        NEW, OPEN, CLOSED
    }

    private enum ResourceType {
        ANDROID_ASSETS, ANDROID_RESOURCE, LINUX_FILESYSTEM, NETWORK, INPUT_STREAM
    }

    /*
     * Instance members
     */

    private InputStream stream = null;
    private StreamStates streamState;

    // Save parameters, for hashCode() and equals()
    private final String filePath;
    private final int resourceId;
    private final String assetPath;
    // For hint to Assimp
    private String resourceFilePath;
    private final URL url;
    private boolean enableUrlLocalCache = false;
    
    private Context context = null;
    private ResourceType resourceType;
    private String inputStreamName;

    /**
     * Open any file you have permission to read.
     * 
     * @param path
     *            A Linux file path
     * 
     * @throws FileNotFoundException
     *             File doesn't exist, or can't be read.
     */
    public GVRAndroidResource(String path) throws FileNotFoundException {
        streamState = StreamStates.NEW;

        filePath = path;
        resourceId = 0; // No R.whatever field will ever be 0
        assetPath = null;
        resourceFilePath = null;
        url = null;
        resourceType = ResourceType.LINUX_FILESYSTEM;
    }

    /**
     * Open any file you have permission to read.
     * 
     * @param file
     *            A Java {@link File} object
     * 
     * @throws FileNotFoundException
     *             File doesn't exist, or can't be read.
     */
    public GVRAndroidResource(File file) throws FileNotFoundException {
        this(file.getAbsolutePath());
    }

    /**
     * Open a {@code res/raw} or {@code res/drawable} bitmap file.
     * 
     * @param gvrContext
     *            The GVR Context
     * @param resourceId
     *            A {@code R.raw} or {@code R.drawable} id
     */
    public GVRAndroidResource(GVRContext gvrContext, int resourceId) {
        this(gvrContext.getContext(), resourceId);
    }

    /**
     * Open a {@code res/raw} or {@code res/drawable} bitmap file.
     * 
     * @param context
     *            An Android Context
     * @param resourceId
     *            A {@code R.raw} or {@code R.drawable} id
     */
    public GVRAndroidResource(Context context, int resourceId) {
        this.context = context;
        Resources resources = context.getResources();
        streamState = StreamStates.NEW;

        filePath = null;
        this.resourceId = resourceId;
        assetPath = null;
        url = null;
        TypedValue value = new TypedValue();
        resources.getValue(resourceId, value, true);
        resourceFilePath = value.string.toString();
        resourceType = ResourceType.ANDROID_RESOURCE;
    }

    /**
     * Open an {@code assets} file
     * 
     * @param gvrContext
     *            The GVR Context
     * @param assetRelativeFilename
     *            A filename, relative to the {@code assets} directory. The file
     *            can be in a sub-directory of the {@code assets} directory:
     *            {@code "foo/bar.png"} will open the file
     *            {@code assets/foo/bar.png}
     * @throws IOException
     *             File does not exist or cannot be read
     */
    public GVRAndroidResource(GVRContext gvrContext,
            String assetRelativeFilename) throws IOException {
        this(gvrContext.getContext(), assetRelativeFilename);
    }

    /**
     * Open an {@code assets} file
     * 
     * @param context
     *            An Android Context
     * @param assetRelativeFilename
     *            A filename, relative to the {@code assets} directory. The file
     *            can be in a sub-directory of the {@code assets} directory:
     *            {@code "foo/bar.png"} will open the file
     *            {@code assets/foo/bar.png}
     * @throws IOException
     *             File does not exist or cannot be read
     */
    public GVRAndroidResource(Context context, String assetRelativeFilename)
            throws IOException {
        this.context = context;    
        streamState = StreamStates.NEW;

        filePath = null;
        resourceId = 0; // No R.whatever field will ever be 0
        assetPath = assetRelativeFilename;
        resourceFilePath = null;
        url = null;
        resourceType = ResourceType.ANDROID_ASSETS;
    }

    /**
     * Open resource from a URL.
     *
     * @param url
     *            A Java {@link URL} object
     * @throws IOException
     */
    public GVRAndroidResource(GVRContext context, URL url) throws IOException {
        this(context, url, false);
    }

    /**
     * Create a resource from an {@link InputStream}.
     *
     * Note that this constructor is package private and is only used by the {@link ZipLoader}
     * (for now).
     *
     * @param name        a String for uniquely identifying this resource
     * @param inputStream an already open {@link InputStream} for this resource
     */
    GVRAndroidResource(String name, InputStream inputStream) {
        inputStreamName = name;
        stream = inputStream;
        streamState = StreamStates.NEW;

        filePath = null;
        resourceId = 0; // No R.whatever field will ever be 0
        assetPath = null;
        resourceFilePath = null;
        url = null;
        resourceType = ResourceType.INPUT_STREAM;
    }

    /*
     * A {@link URLBufferedInputStream} that supports {@link
     * InputStream#mark(int)} and {@link InputStream#reset()}
     */
    static class URLBufferedInputStream extends InputStream {
        private URL url;
        private BufferedInputStream in;

        public URLBufferedInputStream(URL url) throws IOException {
            this.url = url;
            in = new BufferedInputStream(url.openStream());
        }

        @Override
        public boolean markSupported() {
            return true;
        }

        @Override
        public void reset() throws IOException {
            // Since the bufferInputStream resets the offset within the internal
            // buffer while may not resets the url's stream correctly
            // it could easily causes OOM error when decoding it after some
            // initial read and reset.
            // Here we tackle it by opening a new connection to the url and
            // restart from beginning
            in = new BufferedInputStream(url.openStream());
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }

        @Override
        public int read(byte[] buffer, int byteOffset, int byteCount)
                throws IOException {
            return in.read(buffer, byteOffset, byteCount);
        }
    }

    /**
     * Download resource from a URL and open it as a regular file.
     * 
     * @param context
     *            An Android Context
     * @param url
     *            A Java {@link URL} object
     * @throws IOException
     */
    public GVRAndroidResource(GVRContext context, URL url,
            boolean enableUrlLocalCache) throws IOException {
        this.enableUrlLocalCache = enableUrlLocalCache;

        streamState = StreamStates.NEW;
        filePath = null;
        resourceId = 0;
        assetPath = null;
        resourceFilePath = null;
        this.url = url;
        resourceType = ResourceType.NETWORK;
    }

    /**
     * Get the open stream.
     * 
     * Changes the debug state (visible <i>via</i> {@link #toString()}) to
     * {@linkplain GVRAndroidResource.StreamStates#READING READING}.
     * 
     * @return An open {@link InputStream}.
     * @throws IOException 
     */
    public synchronized final InputStream getStream() {
        if (streamState != StreamStates.OPEN) {
            openStream();
        }

        return stream;
    }

    /*
     * TODO Should we somehow expose the CLOSED state? Return null or throw an
     * exception from getStream()? Or is it enough for the calling code to fail,
     * reading a closed stream?
     */

    /**
     * Close the open stream.
     * 
     * Close the stream if it was opened before
     * 
     */
    public synchronized final void closeStream() {
        try {
            if (streamState == StreamStates.OPEN) {
                stream.close();
                stream = null;
            }
            streamState = StreamStates.CLOSED;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     * Open the input stream for resource decoding
     * 
     * 
     */
    public synchronized void openStream() {
        try {
            switch (resourceType) {
            case ANDROID_ASSETS:
                stream = context.getResources().getAssets().open(assetPath);
                streamState = StreamStates.OPEN;
                break;

            case ANDROID_RESOURCE:
                stream = context.getResources().openRawResource(resourceId);
                streamState = StreamStates.OPEN;
                break;

            case LINUX_FILESYSTEM:
                stream = new MarkingFileInputStream(filePath);
                streamState = StreamStates.OPEN;
                break;

            case NETWORK:
                if (!enableUrlLocalCache) {
                    Log.d(TAG,
                            "Do not allow local caching, use streaming to get the resource");
                    stream = new URLBufferedInputStream(url);
                    streamState = StreamStates.OPEN;
                } else {
                    Log.d(TAG,
                            "Allow local caching, download the resource to local cache");
                    File file = GVRAssetLoader.downloadFile(context,
                            url.toString());
                    stream = new MarkingFileInputStream(file);
                    streamState = StreamStates.OPEN;
                }
                break;

            case INPUT_STREAM:
                //input stream is already open
                streamState = StreamStates.OPEN;
                break;
            default:
                stream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save the stream position, for later use with {@link #reset()}.
     * 
     * All {@link GVRAndroidResource} streams support
     * {@link InputStream#mark(int) mark()} and {@link InputStream#reset()
     * reset().} Calling {@link #mark()} right after construction will allow you
     * to read the header then {@linkplain #reset() rewind the stream} if you
     * can't handle the file format.
     * @throws IOException 
     * 
     * @since 1.6.7
     */
    private void mark() throws IOException {
        if (streamState == StreamStates.OPEN) {
            if (stream.markSupported()) {
                stream.mark(Integer.MAX_VALUE);
            } else {
                // In case a inputStream (e.g., fileInputStream) doesn't support
                // mark, throw a exception
                throw new IOException("Input stream doesn't support mark");
            }
        }
    }

    /**
     * Restore the stream position, to the point set by a previous
     * {@link #mark() mark().}
     * 
     * Please note that calling {@link #reset()} generally 'consumes' the
     * {@link #mark()} - <em>do not</em> call
     * 
     * <pre>
     * mark();
     * reset();
     * reset();
     * </pre>
     * @throws IOException 
     * 
     * @since 1.6.7
     */
    private void reset() throws IOException {
        if (streamState == StreamStates.OPEN) {
            if (stream.markSupported()) {
                stream.reset();
            } else {
                // In case a inputStream (e.g., fileInputStream) doesn't support
                // mark, throw a exception
                throw new IOException("Input stream doesn't support mark");
            }
        }
    }

    /**
     * Returns the filename of the resource with extension.
     * 
     * @return Filename of the GVRAndroidResource. May return null if the
     *         resource is not associated with any file
     */
    public String getResourceFilename() {
        switch (resourceType) {
        case ANDROID_ASSETS:
            return assetPath
                    .substring(assetPath.lastIndexOf(File.separator) + 1);

        case ANDROID_RESOURCE:
            return resourceFilePath.substring(
                    resourceFilePath.lastIndexOf(File.separator) + 1);

        case LINUX_FILESYSTEM:
            return filePath.substring(filePath.lastIndexOf(File.separator) + 1);

        case NETWORK:
            return url.getPath().substring(url.getPath().lastIndexOf("/") + 1);

        case INPUT_STREAM:
            return inputStreamName;

        default:
            return null;
        }
    }

    /*
     * Auto-generated hashCode() and equals(), for container support &c.
     * 
     * These check only the private 'parameter capture' fields - not the
     * InputStream.
     */

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((assetPath == null) ? 0 : assetPath.hashCode());
        result = prime * result
                + ((filePath == null) ? 0 : filePath.hashCode());
        result = prime * result
                + ((url == null) ? 0 : url.hashCode());
        result = prime * result
                + ((inputStreamName == null) ? 0 : inputStreamName.hashCode());
        result = prime * result + resourceId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        GVRAndroidResource other = (GVRAndroidResource) obj;
        switch (resourceType) {
        case ANDROID_ASSETS:
            return assetPath.equals(other.assetPath);

        case ANDROID_RESOURCE:
            return resourceId == other.resourceId;

        case LINUX_FILESYSTEM:
            return filePath.equals(other.filePath);

        case NETWORK:
            return url.equals(other.url);

        case INPUT_STREAM:
            return inputStreamName.equals(other.inputStreamName);

        default:
            return false;
        }
    }

    /*
     * toString(), for debugging.
     */

    /**
     * For debugging: shows which file the instance describes, and shows the
     * OPEN / READING / CLOSED state of the input stream.
     */
    @Override
    public String toString() {
        return String.format("%s{filePath=%s; resourceId=%x; assetPath=%s, url=%s, " +
                "inputStreamName=%s}",streamState, filePath, resourceId, assetPath, url,
                inputStreamName);
    }

    /*
     * Generic callback interfaces, for asynchronous APIs that take a {@link
     * GVRAndroidResource} parameter
     */

    /**
     * Callback interface for asynchronous resource loading.
     * 
     * None of the asynchronous resource [textures, and meshes] loading methods
     * that take a {@link GVRAndroidResource} parameter return a value. You must
     * supply a copy of this interface to get results.
     * 
     * <p>
     * While you will often create a callback for each load request, the APIs do
     * each include the {@link GVRAndroidResource} that you are loading. This
     * lets you use the same callback implementation with multiple resources.
     */
    public interface Callback<T extends GVRHybridObject> {
        /**
         * Resource load succeeded.
         * 
         * @param resource
         *            A new GVRF resource
         * @param androidResource
         *            The description of the resource that was loaded
         *            successfully
         */
        void loaded(T resource, GVRAndroidResource androidResource);

        /**
         * Resource load failed.
         * 
         * @param t
         *            Error information
         * @param androidResource
         *            The description of the resource that could not be loaded
         */
        void failed(Throwable t, GVRAndroidResource androidResource);
    }

    /**
     * Callback interface for cancelable resource loading.
     * 
     * Loading uncompressed textures (Android {@linkplain Bitmap bitmaps}) can
     * take hundreds of milliseconds and megabytes of memory; loading even
     * moderately complex meshes can be even slower. GVRF uses a throttling
     * system to manage system load, and an priority system to give you some
     * control over the throttling. This means that slow resource loads can take
     * enough time that you don't actually need the resource by the time the
     * system gets to it. The {@link #stillWanted(GVRAndroidResource)} method
     * lets you cancel unneeded loads.
     */
    public interface CancelableCallback<T extends GVRHybridObject> extends
            Callback<T> {
        /**
         * Do you still want this resource?
         * 
         * If the throttler has a thread available, your request will be run
         * right away; this method will not be called. If not, it is enqueued;
         * this method will be called (at least once) before starting to load
         * the resource. If you no longer need it, returning {@code false} can
         * save non-trivial amounts of time and memory.
         * 
         * @param androidResource
         *            The description of the resource that is about to be loaded
         * 
         * @return {@code true} to continue loading; {@code false} to abort.
         *         (Returning {@code false} will not call
         *         {@link #failed(Throwable, GVRAndroidResource) failed().})
         */
        boolean stillWanted(GVRAndroidResource androidResource);
    }

    /*
     * Specialized callback interfaces, to make use a bit smaller and clearer.
     */

    /**
     * Callback for asynchronous compressed-texture loads.
     * 
     * Compressed textures load very quickly, so they're not subject to
     * throttling and don't support
     * {@link BitmapTextureCallback#stillWanted(GVRAndroidResource)}
     */
    public interface CompressedTextureCallback extends Callback<GVRTexture> {
    }

    /** Callback for asynchronous bitmap-texture loads. */
    public interface BitmapTextureCallback extends
            CancelableCallback<GVRTexture> {
    }

    /**
     * Callback for asynchronous texture loads.
     * 
     * Both compressed and bitmapped textures, using the
     * {@link GVRContext#loadTexture(GVRAndroidResource.TextureCallback, GVRAndroidResource)}
     * APIs.
     * 
     * @since 1.6.7
     */
    public interface TextureCallback extends CancelableCallback<GVRTexture> {
    }

    /** Callback for asynchronous mesh loads */
    public interface MeshCallback extends CancelableCallback<GVRMesh> {
    }

    private GVRCompressedTextureLoader compressedLoader = null;
    private boolean isCompressedTextureSniffed = false;

    public synchronized GVRCompressedTextureLoader getCompressedLoader() {
        if (!isCompressedTextureSniffed) {
            try {
                Log.d(TAG, "Looking for compressed header");
                openStream();
                mark();
                try {
                    compressedLoader = CompressedTexture.sniff(getStream());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    reset();
                    //don't close for this type
                    if (resourceType != ResourceType.INPUT_STREAM) {
                        closeStream();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isCompressedTextureSniffed = true;
            }
        }

        return compressedLoader;
    }
}
