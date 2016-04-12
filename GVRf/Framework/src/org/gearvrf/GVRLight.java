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

import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.utility.TextFile;

public class GVRLight extends GVRPointLight 
{
    protected static String mPointLightShaderSource = null;
    public GVRLight (GVRContext gvrContext, GVRSceneObject owner) {
        super(gvrContext, owner);
    }
    
    public GVRLight (GVRContext gvrContext) {
        this(gvrContext, null);
    }
    
    /**
     * Get the light position uniform.
     * 
     * By convention, GVRF shaders can use a {@code vec3} uniform named
     * {@code light_pos}. With the {@linkplain GVRShaderType.Lit 'lit' shader,}
     * this allows you to add an overlay color on top of the texture.
     * 
     * @return The current {@code vec4 light_pos} as a three-element array
     */
    public float[] getPosition() {
        return getVec3("position");
    }

    /**
     * Set the {@code light_pos} uniform for light.
     * 
     * By convention, GVRF shaders can use a {@code vec3} uniform named
     * {@code light_pos}. With the {@linkplain GVRShaderType.Lit 'lit' shader,}
     * this allows you to add an overlay lighting color on top of the texture.
     * The position is in the camera coordinate system. The user is responsible
     * to use the correct coordinate system.
     * 
     * @param x
     *            x-coordinate in camera coordinate system
     * @param y
     *            y-coordinate in camera coordinate system
     * @param z
     *            z-coordinate in camera coordinate system
     */
    public void setPosition(float x, float y, float z) {
        setVec3("position", x, y, z);
    }

}