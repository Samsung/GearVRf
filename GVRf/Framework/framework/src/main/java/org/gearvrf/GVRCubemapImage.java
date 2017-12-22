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

import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import static android.opengl.GLES30.GL_RGBA;

/**
 * Describes an uncompressed cubemap texture with bitmaps for 6 faces.
 * <p>
 * A cubemap texture supplies individual textures for
 * each of the 6 faces of a cube. It is typically used
 * as a skybox or environment map.
 * <p>
 * The bitmaps for each face must be the same size and they
 * should contain uncompressed data. This type of texture is inefficient because
 * it wastes GPU memory. Mobile GPUs can directly render from
 * compressed textures which use far less memory.
 * @see GVRCompressedCubemapImage
 */
public class GVRCubemapImage extends GVRImage
{
    /**
     * Constructs a cube map texture using six pre-existing {@link Bitmap}s and
     * the user defined filters {@link GVRTextureParameters}.
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     * @param bitmapArray
     *            A {@link Bitmap} array which contains six {@link Bitmap}s. The
     *            six bitmaps correspond to +x, -x, +y, -y, +z, and -z faces of
     *            the cube map texture respectively. The default names of the
     *            six images are "posx.png", "negx.png", "posy.png", "negx.png",
     *            "posz.png", and "negz.png", which can be changed by calling
     *            {@link GVRCubemapImage#setFaceNames(String[])}.
     */
    public GVRCubemapImage(GVRContext gvrContext, Bitmap[] bitmapArray)
    {
        super(gvrContext, NativeBitmapImage.constructor(ImageType.CUBEMAP.Value, GL_RGBA));
        update(bitmapArray);
    }

    public void update(Bitmap[] bitmapArray)
    {
        NativeCubemapImage.update(getNative(), bitmapArray);
    }

    /**
     * Set the names of six images in the zip file. The default names of the six
     * images are "posx.png", "negx.png", "posy.png", "negx.png", "posz.png",
     * and "negz.png". If the names of the six images in the zip file are
     * different to the default ones, this function must be called before load
     * the zip file.
     * 
     * @param nameArray
     *            An array containing six strings which are names of images
     *            corresponding to +x, -x, +y, -y, +z, and -z faces of the cube
     *            map texture respectively.
     */
    public static void setFaceNames(String[] nameArray)
    {
        if (nameArray.length != 6)
        {
            throw new IllegalArgumentException("nameArray length is not 6.");
        }
        for (int i = 0; i < 6; i++)
        {
            faceIndexMap.put(nameArray[i], i);
        }
    }

    final static Map<String, Integer> faceIndexMap = new HashMap<String, Integer>(6);
    static
    {
        /*
         *  File extensions can be .png for uncompressed cubemap textures,
         *  or .pkm for compressed cubemap textures.
         */
        faceIndexMap.put("posx", 0);
        faceIndexMap.put("negx", 1);
        faceIndexMap.put("posy", 2);
        faceIndexMap.put("negy", 3);
        faceIndexMap.put("posz", 4);
        faceIndexMap.put("negz", 5);
    }
}

class NativeCubemapImage
{
    static native void update(long pointer, Bitmap[] bitmapArray);
    static native void updateCompressed(long pointer, int width, int height, int imageSize, byte[][] data, int[] dataOffsets);
}