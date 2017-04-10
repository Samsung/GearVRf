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

import static android.opengl.GLES30.*;

import org.gearvrf.utility.RuntimeAssertion;

import android.util.SparseArray;
import android.util.SparseIntArray;

class EricssonTextureCompression2 extends GVRCompressedTextureLoader {

    private static final int PKM_SIGNATURE = 0x204d4b50; // "PKM "
    private static final int VERSION2_SIGNATURE = 0x00003032; // "20\u0000\u0000"
    private static final int VERSION1_SIGNATURE = 0x00003031; // "10\u0000\u0000"

    @Override
    public int headerLength() {
        return 16;
    }

    @Override
    public boolean sniff(byte[] data, Reader reader) {
        int firstWord = reader.read(4);
        if (firstWord != PKM_SIGNATURE) {
            return false;
        }
        int secondWord = reader.read(3); // last byte varies
        boolean isETC2 = (secondWord == VERSION2_SIGNATURE);
        boolean isETC1 = (secondWord == VERSION1_SIGNATURE);
        boolean matched = (isETC1 || isETC2);
        return matched;
    }

    @Override
    public CompressedTexture parse(byte[] data, Reader reader) {
        // paddedWidthMSB = data[8];
        // paddedWidthLSB = data[9];
        // paddedHeightMSB = data[10];
        // paddedHeightLSB = data[11];
        // widthMSB = data[12];
        // widthLSB = data[13];
        // heightMSB = data[14];
        // heightLSB = data[15];

        reader.skip(7); // skip the signature

        int formatCode = reader.read(1);
        int mapIndex = formatMap.indexOfKey(formatCode);
        if (mapIndex < 0) {
            throw new RuntimeAssertion("Unexpected ETC2 format code %d",
                    formatCode);
        }
        int internalformat = formatMap.valueAt(mapIndex);

        // int paddedWidth = reader.readBE(2);
        // int paddedHeight = reader.readBE(2);
        reader.skip(4);
        int width = reader.readBE(2);
        int height = reader.readBE(2);

        // http://malideveloper.arm.com/downloads/deved/tutorial/SDK/android/1.6/etc_texture.html
        int multiplier;
        switch (internalformat) {
        case GL_COMPRESSED_RG11_EAC:
        case GL_COMPRESSED_SIGNED_RG11_EAC:
        case GL_COMPRESSED_RGBA8_ETC2_EAC:
        case GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC:
            multiplier = 16;
            break;
        default:
            multiplier = 8;
        }
        int imageSize = etc2(width) * etc2(height) * multiplier;

        return CompressedTexture(internalformat, width, height, imageSize, 1,
                data, 16, data.length - 16);
    }

    private int etc2(int dimension) {
        return (dimension >> 2) + ((dimension & 0x3) == 0 ? 0 : 1);
    }

    private final static SparseArray<String> formatNames = new SparseArray<String>(
            8);
    static {
        formatNames.put(GL_COMPRESSED_RGB8_ETC2, "GL_COMPRESSED_RGB8_ETC2");
        formatNames.put(GL_COMPRESSED_RGB8_ETC2, "GL_COMPRESSED_RGB8_ETC2");
        formatNames.put(GL_COMPRESSED_RGBA8_ETC2_EAC,
                "GL_COMPRESSED_RGBA8_ETC2_EAC");
        formatNames.put(GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2,
                "GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2");
        formatNames.put(GL_COMPRESSED_R11_EAC, "GL_COMPRESSED_R11_EAC");
        formatNames.put(GL_COMPRESSED_RG11_EAC, "GL_COMPRESSED_RG11_EAC");
        formatNames.put(GL_COMPRESSED_SIGNED_R11_EAC,
                "GL_COMPRESSED_SIGNED_R11_EAC");
        formatNames.put(GL_COMPRESSED_SIGNED_RG11_EAC,
                "GL_COMPRESSED_SIGNED_RG11_EAC");
    }

    private final static SparseIntArray formatMap = new SparseIntArray(8);
    static {
        // https://github.com/paulvortex/RwgTex/blob/master/libs/etcpack/source/etcpack.cxx
        formatMap.put(0x00, GL_COMPRESSED_RGB8_ETC2);
        formatMap.put(0x01, GL_COMPRESSED_RGB8_ETC2);
        formatMap.put(0x03, GL_COMPRESSED_RGBA8_ETC2_EAC);
        formatMap.put(0x04, GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2);
        formatMap.put(0x05, GL_COMPRESSED_R11_EAC);
        formatMap.put(0x06, GL_COMPRESSED_RG11_EAC);
        formatMap.put(0x07, GL_COMPRESSED_SIGNED_R11_EAC);
        formatMap.put(0x08, GL_COMPRESSED_SIGNED_RG11_EAC);
    }

}
