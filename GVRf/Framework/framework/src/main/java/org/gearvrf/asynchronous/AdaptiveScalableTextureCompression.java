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

import static org.gearvrf.asynchronous.GLESX.*;

import org.gearvrf.utility.RuntimeAssertion;

/** .astc loader */
class AdaptiveScalableTextureCompression extends GVRCompressedTextureLoader {

    // struct astc_header
    // {
    // uint8_t magic [ 4 ];
    // uint8_t blockdim_x;
    // uint8_t blockdim_y;
    // uint8_t blockdim_z ;
    // uint8_t xsize [ 3 ];
    // uint8_t ysize [ 3 ];
    // uint8_t zsize [ 3 ];
    // };

    private static final int MAGIC_NUMBER = 0x5CA1AB13;

    @Override
    public int headerLength() {
        return 16;
    }

    @Override
    public boolean sniff(byte[] data, Reader reader) {
        int magic = reader.read(4);
        return magic == MAGIC_NUMBER;
    }

    @Override
    public CompressedTexture parse(byte[] data, Reader reader) {
        reader.skip(4);
        int blockdim_x = reader.read(1);
        int blockdim_y = reader.read(1);
        int format = getFormat(blockdim_x, blockdim_y);
        if (format == 0) {
            throw new RuntimeAssertion("%dx%d is not a valid ASTC block size",
                    blockdim_x, blockdim_y);
        }
        reader.skip(1); // blockdim_z
        int width = reader.read(3);
        int height = reader.read(3);

        return CompressedTexture(format, width, height, data.length - 16, 1,
                data, 16, data.length - 16);
    }

    /**
     * Smallest entry is 4x4: subtract {@link #OFFSET} from both blockdim_x and
     * blockdim_y to read/write {@link #formatMap}
     */
    private static final int OFFSET = 4;
    /**
     * Maps blockdim_x and blockdim_y to GL_COMPRESSED_?_ASTC_?_KHR constants.
     */
    private static final int[][] formatMap = new int[9][9];

    private static final void setFormat(int blockdim_x, int blockdim_y,
            int format) {
        formatMap[blockdim_x - OFFSET][blockdim_y - OFFSET] = format;
    }

    private static final int getFormat(int blockdim_x, int blockdim_y) {
        return formatMap[blockdim_x - OFFSET][blockdim_y - OFFSET];
    }

    static {
        setFormat(4, 4, GL_COMPRESSED_RGBA_ASTC_4x4_KHR);
        setFormat(5, 4, GL_COMPRESSED_RGBA_ASTC_5x4_KHR);
        setFormat(5, 5, GL_COMPRESSED_RGBA_ASTC_5x5_KHR);
        setFormat(6, 5, GL_COMPRESSED_RGBA_ASTC_6x5_KHR);
        setFormat(6, 6, GL_COMPRESSED_RGBA_ASTC_6x6_KHR);
        setFormat(8, 5, GL_COMPRESSED_RGBA_ASTC_8x5_KHR);
        setFormat(8, 6, GL_COMPRESSED_RGBA_ASTC_8x6_KHR);
        setFormat(8, 8, GL_COMPRESSED_RGBA_ASTC_8x8_KHR);
        setFormat(10, 5, GL_COMPRESSED_RGBA_ASTC_10x5_KHR);
        setFormat(10, 6, GL_COMPRESSED_RGBA_ASTC_10x6_KHR);
        setFormat(10, 8, GL_COMPRESSED_RGBA_ASTC_10x8_KHR);
        setFormat(10, 10, GL_COMPRESSED_RGBA_ASTC_10x10_KHR);
        setFormat(12, 10, GL_COMPRESSED_RGBA_ASTC_12x10_KHR);
        setFormat(12, 12, GL_COMPRESSED_RGBA_ASTC_12x12_KHR);
    }
}
