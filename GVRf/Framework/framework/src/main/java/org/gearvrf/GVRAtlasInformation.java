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

/**
 * Contains the information, in UV space, of offset and scale
 * necessary to map a 2D texture of a texture atlas to the GVRSceneObject.
 */
public class GVRAtlasInformation {
    private String mName = null;
    private float[] mOffset = {0, 0};
    private float[] mScale = {1, 1};

    /**
     * Contructs a Altas information in UV space of a atlased object.
     *
     * @param name Name of the atlased object.
     * @param offset UV space of the position of a atlased object into the texture.
     * @param scale UV space of the scale of a atlased object into the texture.
     */
    public GVRAtlasInformation(String name, float[] offset, float[] scale) {
        mName = name;
        mOffset = offset;
        mScale = scale;
    }

    /**
     * The name of the atlased object. Use it to
     * get find the atlased object into the scene.
     *
     * @return The name of a atlased object.
     */
    public String getName() {
        return mName;
    }

    /**
     * Offset in uv space of a atlased object into the texture.
     *
     * @return array with uv coord of a atlased object.
     */
    public float[] getOffset() {
        return mOffset;
    }

    /**
     * Scale in uv space of a atlased object into the texture.
     *
     * @return array with uv scale of a atlased object.
     */
    public float[] getScale() {
        return mScale;
    }
}
