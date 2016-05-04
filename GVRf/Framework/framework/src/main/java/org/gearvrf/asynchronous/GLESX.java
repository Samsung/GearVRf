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

import android.opengl.GLES30;

/**
 * Primarily declarations for the GL_KHR_texture_compression_astc_ldr (and
 * GL_KHR_texture_compression_astc_hdr?) extension(s).
 */
abstract class GLESX extends GLES30 {

    /**
     * If {@code glGetString(GL_EXTENSIONS)} does not contain
     * {@code GL_ASTC_EXTENSION}, the device does not support astc compressed
     * textures.
     */
    public static final String GL_ASTC_EXTENSION = "GL_KHR_texture_compression_astc_hdr";

    public static final int GL_COMPRESSED_RGBA_ASTC_4x4_KHR = 0x93B0;
    public static final int GL_COMPRESSED_RGBA_ASTC_5x4_KHR = 0x93B1;
    public static final int GL_COMPRESSED_RGBA_ASTC_5x5_KHR = 0x93B2;
    public static final int GL_COMPRESSED_RGBA_ASTC_6x5_KHR = 0x93B3;
    public static final int GL_COMPRESSED_RGBA_ASTC_6x6_KHR = 0x93B4;
    public static final int GL_COMPRESSED_RGBA_ASTC_8x5_KHR = 0x93B5;
    public static final int GL_COMPRESSED_RGBA_ASTC_8x6_KHR = 0x93B6;
    public static final int GL_COMPRESSED_RGBA_ASTC_8x8_KHR = 0x93B7;
    public static final int GL_COMPRESSED_RGBA_ASTC_10x5_KHR = 0x93B8;
    public static final int GL_COMPRESSED_RGBA_ASTC_10x6_KHR = 0x93B9;
    public static final int GL_COMPRESSED_RGBA_ASTC_10x8_KHR = 0x93BA;
    public static final int GL_COMPRESSED_RGBA_ASTC_10x10_KHR = 0x93BB;
    public static final int GL_COMPRESSED_RGBA_ASTC_12x10_KHR = 0x93BC;
    public static final int GL_COMPRESSED_RGBA_ASTC_12x12_KHR = 0x93BD;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR = 0x93D0;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x4_KHR = 0x93D1;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x5_KHR = 0x93D2;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x5_KHR = 0x93D3;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x6_KHR = 0x93D4;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x5_KHR = 0x93D5;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x6_KHR = 0x93D6;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x8_KHR = 0x93D7;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x5_KHR = 0x93D8;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x6_KHR = 0x93D9;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x8_KHR = 0x93DA;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x10_KHR = 0x93DB;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_12x10_KHR = 0x93DC;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_12x12_KHR = 0x93DD;

}
