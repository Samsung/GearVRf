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
 * Name map for material shaders.
 * 
 * <p>
 * A {@link GVRMesh} specifies a {@linkplain GVRSceneObject scene object's}
 * surface geometry; a {@link GVRMaterial} specifies a scene object's surface
 * appearance. A {@link GVRMaterial} specifies a shader by
 * {@link GVRShaderId id}, and contains named values. These names are
 * not necessarily the same as the names of the attributes and uniforms in the
 * shader program: the methods of this class let you map names from materials to
 * programs.
 * @deprecated this entire class does absolutely nothing and is no longer necessary
 */
public class GVRMaterialMap implements GVRShaderMaps
{
    protected long mNativePointer;

    GVRMaterialMap(GVRContext gvrContext, long nativePointer) {
        mNativePointer = nativePointer;
    }

    public void addTextureKey(String variableName, String key) {
    }

    /**
     * Link a float in a material to this GL program.
     * 
     * @param variableName
     *            The variable name in the GL program.
     * @param key
     *            The float key in the material.
     */
    public void addAttributeFloatKey(String variableName, String key) {
    }

    /**
     * Link a vec2 in a material to this GL program.
     * 
     * @param variableName
     *            The variable name in the GL program.
     * @param key
     *            The vec2 key in the material.
     */
    public void addAttributeVec2Key(String variableName, String key) {
    }

    /**
     * Link a vec3 in a material to this GL program.
     * 
     * @param variableName
     *            The variable name in the GL program.
     * @param key
     *            The vec3 key in the material.
     */
    public void addAttributeVec3Key(String variableName, String key) {
    }

    /**
     * Link a vec4 in a material to this GL program.
     * 
     * @param variableName
     *            The variable name in the GL program.
     * @param key
     *            The vec4 key in the material.
     */
    public void addAttributeVec4Key(String variableName, String key) {
    }

    public void addUniformFloatKey(String variableName, String key) {
    }

    public void addUniformVec2Key(String variableName, String key) {
    }

    public void addUniformVec3Key(String variableName, String key) {
     }

    public void addUniformVec4Key(String variableName, String key) {
    }

    /**
     * Link a mat4 in a material to this GL program.
     * 
     * @param variableName
     *            The variable name in the GL program.
     * @param key
     *            The mat4 key in the material.
     */
    public void addUniformMat4Key(String variableName, String key) {
    }
}
