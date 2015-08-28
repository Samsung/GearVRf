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

import static android.opengl.GLES20.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.gearvrf.GVRContext;
import org.gearvrf.utility.Log;
import org.gearvrf.utility.RuntimeAssertion;

import android.opengl.GLES30;

class KTX extends GVRCompressedTextureLoader {

    private static final String TAG = Log.tag(KTX.class);

    // http://www.khronos.org/opengles/sdk/tools/KTX/file_format_spec
    // Byte[12] identifier
    // UInt32 endianness
    // UInt32 glType
    // UInt32 glTypeSize
    // UInt32 glFormat
    // Uint32 glInternalFormat
    // Uint32 glBaseInternalFormat
    // UInt32 pixelWidth
    // UInt32 pixelHeight
    // UInt32 pixelDepth
    // UInt32 numberOfArrayElements
    // UInt32 numberOfFaces
    // UInt32 numberOfMipmapLevels
    // UInt32 bytesOfKeyValueData

    private static final int[] SIGNATURE = {
            // '«', 'K', 'T', 'X', ' ', '1', '1', '»', '\r', '\n', '\x1A', '\n'
            0xAB4B5458, 0x203131BB, 0x0D0A1A0A };

    @Override
    public int headerLength() {
        return (SIGNATURE.length + 13) * Reader.INTEGER_BYTES;
    }

    @Override
    public boolean sniff(byte[] data, Reader reader) {
        for (int chunk : SIGNATURE) {
            if (chunk != reader.readBE(Reader.INTEGER_BYTES)) {
                return false;
            }
        }
        // else
        return true;
    }

    @Override
    public CompressedTexture parse(byte[] data, Reader reader) {
        reader.skip(SIGNATURE.length * Reader.INTEGER_BYTES);

        int endianness = reader.readLE(Reader.INTEGER_BYTES);
        boolean littleEndian;
        switch (endianness) {
        case 0x01020304:
            littleEndian = false;
            break;
        case 0x04030201:
            littleEndian = true;
            break;
        default:
            throw new RuntimeAssertion("Unexpected endianness %08X", endianness);
        }
        Log.d(TAG, "endianness = %08x, littleEndian == %b", endianness,
                littleEndian);

        KtxReader ktxReader = new KtxReader(reader, littleEndian);

        int glType = ktxReader.readInt();
        int glTypeSize = ktxReader.readInt();
        int glFormat = ktxReader.readInt();
        if (glType != 0 | glTypeSize != 1 || glFormat != 0) {
            throw new RuntimeAssertion(
                    "Uncompressed ktx textures not supported, yet: glType = %d, glTypeSize = %x, glFormat = %d",
                    glType, glTypeSize, glFormat);
        }

        int glInternalFormat = ktxReader.readInt();
        int glBaseInternalFormat = ktxReader.readInt();
        int pixelWidth = ktxReader.readInt();
        int pixelHeight = ktxReader.readInt();

        int pixelDepth = ktxReader.readInt();
        if (pixelDepth != 0) {
            throw new RuntimeAssertion("3D textures not supported");
        }

        int numberOfArrayElements = ktxReader.readInt();
        if (numberOfArrayElements != 0) {
            throw new RuntimeAssertion("Array textures not supported");
        }

        int numberOfFaces = ktxReader.readInt();
        if (numberOfFaces != 1) {
            throw new RuntimeAssertion("Cube maps not supported");
        }

        int numberOfMipmapLevels = ktxReader.readInt();
        int bytesOfKeyValueData = ktxReader.readInt();

        Log.d(TAG,
                "glInternalFormat = %x, glBaseInternalFormat = %x, pixelWidth = %d, pixelHeight = %d, numberOfMipmapLevels = %d, bytesOfKeyValueData = %d",
                glInternalFormat, glBaseInternalFormat, pixelWidth,
                pixelHeight, numberOfMipmapLevels, bytesOfKeyValueData);

        // 13 UInt32 plus a Byte[12], plus any key-value pairs
        int headerSize = (SIGNATURE.length + 13) * Reader.INTEGER_BYTES
                + bytesOfKeyValueData;

        ByteBuffer buffer = ByteBuffer.wrap(data, headerSize, data.length
                - headerSize);
        return new KtxCompressedTexture(
        /* glBaseInternalFormat */glInternalFormat, pixelWidth, pixelHeight,
                numberOfMipmapLevels, buffer, littleEndian);
    }

    private static class KtxReader {
        private final Reader reader;
        private final boolean littleEndian;

        private KtxReader(Reader reader, boolean littleEndian) {
            this.reader = reader;
            this.littleEndian = littleEndian;
        }

        int readInt() {
            return littleEndian ? reader.readLE(Reader.INTEGER_BYTES) : reader
                    .readBE(Reader.INTEGER_BYTES);
        }
    }

    private static class KtxCompressedTexture extends CompressedTexture {
        private static final String TAG = Log.tag(KtxCompressedTexture.class);

        private final boolean littleEndian;

        private KtxCompressedTexture(int internalformat, int width, int height,
                int levels, ByteBuffer data, boolean littleEndian) {
            super(internalformat, width, height, -1, levels, data);
            this.littleEndian = littleEndian;
        }

        @Override
        public GVRCompressedTexture toTexture(GVRContext gvrContext, int quality) {
            GVRCompressedTexture result = new GVRCompressedTexture(gvrContext,
                    GVRCompressedTexture.GL_TARGET, levels, quality);

            ByteBuffer data = getData();
            ByteOrder defaultOrder = data.order();
            ByteOrder dataOrder = littleEndian ? ByteOrder.LITTLE_ENDIAN
                    : ByteOrder.BIG_ENDIAN;

            result.rebind();

            for (int fileLevel = 0; fileLevel < levels; ++fileLevel) {
                data.order(dataOrder);
                int imageSize = data.getInt();
                data.order(defaultOrder);

                int imagePadding = (4 - (imageSize & 0x03)) & 0x03;

                Log.d(TAG,
                        "Creating level %d as %dx%d, internalformat = %x; imageSize = %d (imagePadding = %d), position = %d",
                        fileLevel, //
                        width >>> fileLevel, height >>> fileLevel, //
                        internalformat, imageSize, imagePadding, //
                        data.position());

                // Note that this call does NOT advance the Buffer position
                GLES30.glCompressedTexImage2D(GL_TEXTURE_2D, fileLevel,
                        internalformat, Math.max(1, width >> fileLevel),
                        Math.max(1, height >> fileLevel), 0, imageSize, data);
                data.position(data.position() + imageSize + imagePadding);
            }

            result.unbind();
            return result;
        }

    }
}
