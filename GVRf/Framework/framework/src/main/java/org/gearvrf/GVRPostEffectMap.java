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
 * Name map for post effect shaders.
 * 
 * <p>
 * A {@link GVRPostEffect} specifies a shader by {@link GVRPostEffectShaderId
 * id}, and contains named values. These names are not necessarily the same as
 * the names of the uniforms in the shader program: the methods of this class
 * let you map names from materials to programs.
 * 
 * <p>
 * Despite a somewhat narrower API, it fills much the same role as
 * {@link GVRMaterialMap}.
 */
public class GVRPostEffectMap extends GVRHybridObject implements GVRShaderMaps {
    GVRPostEffectMap(GVRContext gvrContext, long ptr) {
        super(gvrContext, ptr);
    }

    public void addTextureKey(String variableName, String key) {
        NativeCustomPostEffectShader.addTextureKey(getNative(), variableName,
                key);
    }

    public void addUniformFloatKey(String variableName, String key) {
        NativeCustomPostEffectShader
                .addFloatKey(getNative(), variableName, key);
    }

    public void addUniformVec2Key(String variableName, String key) {
        NativeCustomPostEffectShader.addVec2Key(getNative(), variableName, key);
    }

    public void addUniformVec3Key(String variableName, String key) {
        NativeCustomPostEffectShader.addVec3Key(getNative(), variableName, key);
    }

    public void addUniformVec4Key(String variableName, String key) {
        NativeCustomPostEffectShader.addVec4Key(getNative(), variableName, key);
    }

    public void addUniformMat4Key(String variableName, String key) {
        NativeCustomPostEffectShader.addMat4Key(getNative(), variableName, key);
    }
}

class NativeCustomPostEffectShader {
    static native void addTextureKey(long customPostEffectShader,
            String variableName, String key);

    static native void addFloatKey(long customPostEffectShader,
            String variableName, String key);

    static native void addVec2Key(long customPostEffectShader,
            String variableName, String key);

    static native void addVec3Key(long customPostEffectShader,
            String variableName, String key);

    static native void addVec4Key(long customPostEffectShader,
            String variableName, String key);

    static native void addMat4Key(long customPostEffectShader,
            String variableName, String key);
}
