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

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.graphics.BitmapFactory;
import android.opengl.GLES20;

/**
 * Implement this class to use compressed texture formats that GVRF does not
 * support.
 * 
 * A {@link GVRCompressedTextureLoader} contains the logic to detect a
 * particular file type, and to parse the header. It is an abstract class (not
 * an interface) so that it can contain a protected method (
 * {@code CompressedTexture()}) that calls the private {@code CompressedTexture}
 * constructor: this limits the chances that someone will create an instance
 * with invalid value, while still allowing apps to add new loaders without
 * having to add them to this package.
 * 
 * <p>
 * The general data flow is
 * <ul>
 * <li>The internal load method loads the file into memory
 * <li>If one and only one {@linkplain #sniff(byte[], Reader) sniff()} method
 * returns {@code true}, the data is passed to the corresponding
 * {@linkplain #parse(byte[], Reader) parse()} method
 * <li>The {@code parse()} method extracts GL parameters, and uses
 * {@code CompressedTexture()} to return an internal {@code CompressedTexture}
 * instance
 * <li>The internal load method passes that {@code CompressedTexture} to a
 * GL-thread callback, that converts it to a texture and passes that texture to
 * the app's {@link org.gearvrf.GVRAndroidResource.BitmapTextureCallback
 * BitmapTextureCallback}
 * </ul>
 * 
 * @since 1.6.1
 */
public abstract class GVRCompressedTextureLoader {
    protected GVRCompressedTextureLoader() {
    }

    /**
     * Bytes of header data that we need to
     * {@link #sniff(byte[], Reader)} or {@link #parse(byte[], Reader)}.
     * 
     * When we <em>know</em> that a file contains a compressed texture, we can
     * simply load the whole thing into a {@code byte[]}, and pass the offset of
     * the actual data to
     * {@link #CompressedTexture(int, int, int, int, int, byte[], int, int)}.
     * But, when a file may contain either an Android {@link Bitmap} or a
     * compressed texture, we don't want to load the whole file into memory:
     * {@link BitmapFactory#decodeStream(InputStream)} is more memory-efficient
     * than {@link BitmapFactory#decodeByteArray(byte[], int, int)}.
     * 
     * @return Number of bytes of header data needed to successfully sniff or
     *         parse the file format.
     * 
     * @since 1.6.6
     */
    public abstract int headerLength();

    /**
     * Does this byte array contain an instance of 'my' compressed texture? The
     * {@link CompressedTexture#load(InputStream) load()} methods will call all
     * registered Loader's sniffers: if one and only one returns {@code true},
     * the load() method will return a {@code CompressedTexture}.
     * 
     * <p>
     * <em>Note:</em> This routine needs to be very fast! The
     * {@link CompressedTexture#load(InputStream) load()} routine will call all
     * registered sniffers, rather than looking at (possibly invalid) file
     * extensions, or asking the user for a (possibly invalid) hint.
     * 
     * @param data
     *            A compressed texture file's contents
     * @param reader
     *            A data reader, pointing to data[0]
     * @return Whether or not this data is in 'my' format
     */
    public abstract boolean sniff(byte[] data, Reader reader);

    /**
     * Parse the header, and return a {@link CompressedTexture}. This will only
     * be called if the loader's {@link #sniff(byte[], Reader)} function
     * returned {@code true}.
     * 
     * @param data
     *            A compressed texture file's contents: this loader's
     *            {@link #sniff(byte[], Reader)} function has already returned
     *            {@code true}.
     * @param reader
     *            A data reader, pointing to data[0]
     * @return A {@code CompressedTexture}, from
     *         {@link #CompressedTexture(int, int, int, int, int, byte[], int, int)}
     */
    public abstract CompressedTexture parse(byte[] data, Reader reader);

    /**
     * Provides external parsers access to the internal
     * {@code CompressedTexture} constructor.
     * 
     * The {@code CompressedTexture} class represents a texture file, loaded
     * into memory; it's what your {@link #parse(byte[], Reader)} method needs
     * to return.
     * 
     * <p>
     * The first four parameters are passed directly to
     * {@code glCompressedTexImage2D}; the names are from <a href=
     * "https://www.khronos.org/opengles/sdk/docs/man/xhtml/glCompressedTexImage2D.xml"
     * >https://www.khronos.org/opengles/sdk/docs/man/xhtml/
     * glCompressedTexImage2D.xml</a>
     * 
     * @param internalformat
     *            The
     *            {@link GLES20#glCompressedTexImage2D(int, int, int, int, int, int, int, java.nio.Buffer)
     *            glCompressedTexImage2D()} <code>internalformat</code>
     *            parameter.
     * @param width
     *            The
     *            {@link GLES20#glCompressedTexImage2D(int, int, int, int, int, int, int, java.nio.Buffer)
     *            glCompressedTexImage2D()} <code>width</code> parameter.
     * @param height
     *            The
     *            {@link GLES20#glCompressedTexImage2D(int, int, int, int, int, int, int, java.nio.Buffer)
     *            glCompressedTexImage2D()} <code>height</code> parameter.
     * @param imageSize
     *            The
     *            {@link GLES20#glCompressedTexImage2D(int, int, int, int, int, int, int, java.nio.Buffer)
     *            glCompressedTexImage2D()} <code>imageSize</code> parameter.
     * @param levels
     *            The number of mipmap levels
     * @param data
     *            The {@code byte[]} passed to {@link #parse(byte[], Reader)}
     * @param dataOffset
     *            Header length - offset of first byte of texture data
     * @param dataBytes
     *            Number of bytes of texture data
     * @return An internal buffer that the GL thread can use to create a
     *         {@link GVRCompressedTexture}
     */
    protected CompressedTexture CompressedTexture(int internalformat,
            int width, int height, int imageSize, int levels, byte[] data,
            int dataOffset, int dataBytes) {
        ByteBuffer buffer = ByteBuffer.wrap(data, dataOffset, dataBytes);
        return new CompressedTexture(internalformat, width, height, imageSize,
                levels, buffer);
    }

    /**
     * Register a loader with the 'sniffer'.
     * 
     * 'Factory loaders' are pre-registered. To load a format we don't support,
     * create a {@link GVRCompressedTextureLoader} descendant. Then, before
     * trying to load any files in that format, create an instance and call
     * {@link #register()}:
     * 
     * <pre>
     * 
     * new MyCompressedFormat().register();
     * </pre>
     */
    public void register() {
        synchronized (loaders) {
            loaders.add(this);

            maximumHeaderLength = 0;
            for (GVRCompressedTextureLoader loader : loaders) {
                int headerLength = loader.headerLength();
                if (headerLength > maximumHeaderLength) {
                    maximumHeaderLength = headerLength;
                }
            }
        }
    }

    static List<GVRCompressedTextureLoader> getLoaders() {
        return loaders;
    }

    private static final List<GVRCompressedTextureLoader> loaders = new ArrayList<GVRCompressedTextureLoader>();
    static int maximumHeaderLength = 0;

    /*
     * We can (and do) expect apps to register any custom loaders before calling
     * one of the load() methods, but we can't have the 'factory loaders'
     * register themselves in their own static initializers: If the only
     * reference to a loader is in its own class, Java may never call its
     * initializer.
     */
    static {
        new AdaptiveScalableTextureCompression().register();
        new EricssonTextureCompression2().register();
        new KTX().register();
    }

    /** Utility class for reading big- and little-endian numbers from a header */
    protected static final class Reader {
        private final byte[] data;
        // private final int length;
        private int readPointer;

        /** Wrap a Reader around a byte array */
        protected Reader(byte[] data) {
            this.data = data;
            // this.length = data.length;
            this.readPointer = 0;
        }

        private byte read() {
            return data[readPointer++];
        }

        protected static final int INTEGER_BYTES = Integer.SIZE / Byte.SIZE;
        protected static final int SHORT_BYTES = Short.SIZE / Byte.SIZE;

        /**
         * Read an int
         * 
         * @param bytes
         *            Should be in range 1..4. Not checked, and bad things can
         *            happen if you pass invalid values!
         * @return A little-endian number from the byte array
         */
        protected int read(int bytes) {
            return readLE(bytes);
        }

        /**
         * Read a little-endian int
         * 
         * @param bytes
         *            Should be in range 1..4. Not checked, and bad things can
         *            happen if you pass invalid values!
         * @return A little-endian number from the byte array
         */
        protected int readLE(int bytes) {
            int result = 0;
            for (int index = 0; index < bytes; ++index) {
                result |= ((int) read() & 0xff) << (index * Byte.SIZE);
            }
            return result;
        }

        /*
         * readBE() is slightly cheaper than readLE(): readBE() takes 4
         * shift-left-by-8 ops to read an integer, while readLE() takes a
         * shift-by-0, a shift-by-8, a shift-by-16, and a shift-by-24.
         * 
         * This suggests that it may make sense to implement readLE() as
         * Integer.reverseBytes(readBE(bytes)) - but simple benchmarking shows
         * that this is actually a bit slower. (Integer.reverseBytes() is
         * cleverly implemented, but it still takes 4 shifts, 3 |s, and 2 &s.)
         */

        /**
         * Read a big-endian int
         * 
         * @param bytes
         *            Should be in range 1..4. Not checked, and bad things can
         *            happen if you pass invalid values!
         * @return A big-endian number from the byte array
         */
        protected int readBE(int bytes) {
            int result = 0;
            for (int index = 0; index < bytes; ++index) {
                result = (result << Byte.SIZE) | ((int) read() & 0xff);
            }
            return result;
        }

        /**
         * Read a long
         * 
         * @param bytes
         *            Should be in range 1..8. Not checked, and bad things can
         *            happen if you pass invalid values!
         * @return A little-endian number from the byte array
         * @deprecated Sniffers need to be very fast!
         */
        protected long readLong(int bytes) {
            long result = 0L;
            for (int index = 0; index < bytes; ++index) {
                result |= ((long) read() & 0xffL) << (index * Byte.SIZE);
            }
            return result;
        }

        /** Advance the read pointer */
        protected void skip(int bytes) {
            readPointer += bytes;
        }

        /** Set the read pointer to the start of the stream */
        protected void reset() {
            readPointer = 0;
        }

        /**
         * Get the current value of the read pointer. In conjunction with
         * {@link #setPosition(int)}, this can be used to 'read ahead'.
         */
        protected int getPosition() {
            return readPointer;
        }

        /**
         * Set the value of the read pointer. This can be used to return to a
         * previous {@link #getPosition()} after 'reading ahead'.
         */
        protected void setPosition(int position) {
            readPointer = position;
        }
    }
}
