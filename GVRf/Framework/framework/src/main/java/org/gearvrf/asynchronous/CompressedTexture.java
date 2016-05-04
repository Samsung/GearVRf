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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.asynchronous.GVRCompressedTextureLoader.Reader;

import android.opengl.GLES20;

/**
 * Represents a texture file, loaded into memory. Pass to EGL by calling
 * {@link #toTexture(GVRContext, int)}. Instances of this class hold the texture
 * file contents in memory: don't hold onto them any longer than necessary.
 * 
 * <p>
 * Get an instance by calling one of the {@code load()} overloads; register a
 * new loader by calling
 * {@link GVRCompressedTextureLoader#register(GVRCompressedTextureLoader)}.
 * 
 * <p>
 * Note that {@link #toTexture(GVRContext, int)} <em>must</em> be called from
 * the GL thread; other methods may be called from any thread.
 */
public class CompressedTexture {

    // Field names from
    // https://www.khronos.org/opengles/sdk/docs/man/xhtml/glCompressedTexImage2D.xml
    /**
     * The
     * {@link GLES20#glCompressedTexImage2D(int, int, int, int, int, int, int, java.nio.Buffer)
     * glCompressedTexImage2D()} <code>internalformat</code> parameter.
     */
    protected final int internalformat;
    /**
     * The
     * {@link GLES20#glCompressedTexImage2D(int, int, int, int, int, int, int, java.nio.Buffer)
     * glCompressedTexImage2D()} <code>width</code> parameter.
     */
    protected final int width;
    /**
     * The
     * {@link GLES20#glCompressedTexImage2D(int, int, int, int, int, int, int, java.nio.Buffer)
     * glCompressedTexImage2D()} <code>height</code> parameter.
     */
    protected final int height;
    /**
     * The
     * {@link GLES20#glCompressedTexImage2D(int, int, int, int, int, int, int, java.nio.Buffer)
     * glCompressedTexImage2D()} <code>imageSize</code> parameter.
     */
    protected final int imageSize;

    /** The number of mipmap levels in the data */
    protected final int levels;

    /*
     * Currently only support containers with mipmap chains - not containers
     * with unrelated textures.
     */
    private final ByteBuffer data;
    private final int dataOffset;

    protected CompressedTexture(int internalformat, int width, int height,
            int imageSize, int levels, ByteBuffer data) {
        this.internalformat = internalformat;
        this.width = width;
        this.height = height;
        this.imageSize = imageSize;
        this.levels = levels;
        this.data = data;

        /*
         * Initial position is the data offset in backing array. We cannot use
         * ByteBuffer.arrayOffset() which is always 0.
         */
        this.dataOffset = data.position();
    }

    /*
     * Get backing array.
     */
    protected byte[] getArray() {
        return data.array();
    }

    /*
     * Get offset of data in backing array.
     */
    protected int getArrayOffset() {
        return dataOffset;
    }

    /*
     * Get the ByteBuffer.
     */
    protected ByteBuffer getData() {
        return data;
    }

    GVRCompressedTexture toTexture(GVRContext gvrContext, int quality) {
        return new GVRCompressedTexture(gvrContext, internalformat, width,
                height, imageSize, getArray(), getArrayOffset(), levels, quality);
    }

    // Texture parameters
    GVRCompressedTexture toTexture(GVRContext gvrContext, int quality,
            GVRTextureParameters textureParameters) {
        return new GVRCompressedTexture(gvrContext, internalformat, width,
                height, imageSize, getArray(), getArrayOffset(), levels, quality,
                textureParameters);
    }

    /**
     * Loads a file into memory; detects type, and calls the appropriate
     * {@link GVRCompressedTextureLoader#parse(byte[], Reader)} method.
     * 
     * @param stream
     *            InputStream containing a compressed texture file
     * @param maxLength
     *            Max length to read. -1 for unlimited.
     * @param closeStream
     *            Close {@code stream} on exit?
     * @return Normally, one and only one {@link GVRCompressedTextureLoader}
     *         will recognize the file, and this method will return a
     *         {@link CompressedTexture}, ready to be passed to EGL via
     *         {@link CompressedTexture#glCompressedTexImage2D(int, int)}. If no
     *         loaders recognize the file, or if multiple loaders recognize the
     *         file, this method will return <code>null</null>.
     * @throws IOException
     *             Does not catch any internal exceptions
     */
    static CompressedTexture load(InputStream stream, int maxLength,
                                  boolean closeStream)
            throws IOException {
        byte[] data;
        try {
            data = maxLength >= 0
                ? readBytes(stream, maxLength)
                : readBytes(stream);
        } finally {
            if (closeStream) {
                stream.close();
            }
        }

        Reader reader = new Reader(data);

        GVRCompressedTextureLoader valid = null;
        List<GVRCompressedTextureLoader> loaders = GVRCompressedTextureLoader
                .getLoaders();
        synchronized (loaders) {
            for (GVRCompressedTextureLoader loader : loaders) {
                if (loader.sniff(data, reader)) {
                    if (valid != null) {
                        throw new IllegalArgumentException(
                                "Multiple loaders think this smells right");
                    }
                    valid = loader;
                }
                reader.reset();
            }
            if (valid == null) {
                throw new IllegalArgumentException(
                        "No loader thinks this smells right");
            }
            return valid.parse(data, reader);
        }
    }

    public static GVRCompressedTextureLoader sniff(InputStream stream)
            throws IOException {
        byte[] data = readBytes(stream,
                GVRCompressedTextureLoader.maximumHeaderLength);

        Reader reader = new Reader(data);

        GVRCompressedTextureLoader valid = null;
        List<GVRCompressedTextureLoader> loaders = GVRCompressedTextureLoader
                .getLoaders();
        synchronized (loaders) {
            for (GVRCompressedTextureLoader loader : loaders) {
                if (loader.sniff(data, reader)) {
                    if (valid != null) {
                        throw new IllegalArgumentException(
                                "Multiple loaders think this smells right");
                    }
                    valid = loader;
                }
                reader.reset();
            }
            return valid;
        }
    }

    static CompressedTexture parse(InputStream stream, boolean closeStream,
            GVRCompressedTextureLoader loader) throws IOException {
        byte[] data;
        try {
            data = readBytes(stream);
        } finally {
            if (closeStream) {
                stream.close();
            }
        }

        return loader.parse(data, new Reader(data));
    }

    private static byte[] readBytes(InputStream stream, final int bytes)
            throws IOException {
        byte[] result = new byte[bytes], buffer = new byte[bytes];
        int length = 0;

        for (int read = 0; read >= 0 && length < bytes; read = stream
                .read(buffer)) {
            if (read > 0) {
                for (int index = 0; index < read && length < bytes;) {
                    result[length++] = buffer[index++];
                }
            }
        }
        buffer = null;

        return result;
    }

    private static byte[] readBytes(InputStream stream) throws IOException {
        byte[] result = new byte[INITIAL_CAPACITY];
        int capacity = result.length, length = 0;

        byte[] buffer = new byte[BUFFER_SIZE];

        for (int read = 0; read >= 0; read = stream.read(buffer)) {
            if (read > 0) {
                if (length + read > capacity) {
                    // copy to new array with double capacity
                    capacity <<= 1;
                    result = Arrays.copyOf(result, capacity);
                }
                for (int index = 0; index < read; ++index) {
                    result[length++] = buffer[index];
                }
            }
        }
        buffer = null;

        return capacity == length ? result : Arrays.copyOf(result, length);
    }

    private static final int INITIAL_CAPACITY = 4 * 1024;
    private static final int BUFFER_SIZE = 4 * 1024;
}
