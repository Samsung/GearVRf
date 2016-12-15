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

/***************************************************************************
 * Manages instances of shaders.
 ***************************************************************************/

#ifndef SHADER_MANAGER_H_
#define SHADER_MANAGER_H_

#include <mutex>
#include <map>

#include "objects/hybrid_object.h"
#include "shaders/material/custom_shader.h"

namespace gvr {
/**
 * Keeps track of a set of native shaders.
 * A shader can be referenced by the ID given to it by the
 * ShaderManager when it is added. It can also be referenced
 * by its unique signature string provided by the Java layer.
 *
 * There can be more than one shader manager. Usually GearVRF
 * keeps two - one for material shaders and one for post effect shaders.
 * All shaders are global and are maintained between scene changes.
 */
class ShaderManager: public HybridObject {
public:
    ShaderManager() :
            HybridObject(),
            latest_shader_id_(0)
    { }

    ~ShaderManager();

/*
 * Add a native shader to this shader manager.
 * @param signature         Unique signature string
 * @param uniformDescriptor String giving the names and types of shader material uniforms
 *                          This does NOT include uniforms used by light sources
 * @param textureDescriptor String giving the names and types of texture samplers
 * @param vertexDescriptor  String giving the names and types of vertex attributes
 * @param vertexShader      String with GLSL source for vertex shader
 * @param fragmentShader    String with GLSL source for fragment shader
 *
 * This function is called by the Java layer to request generation of a specific
 * vertex / fragment shader pair. If the shader has not already been generated,
 * it description is added to the table.
 * @returns ID of shader (integer that is unique within this ShaderManager).
 */
    int addShader(const std::string& signature,
                  const std::string& uniformDescriptor,
                  const std::string& textureDescriptor,
                  const std::string& vertexDescriptor,
                  const std::string& vertex_shader,
                  const std::string& fragment_shader);

    /*
     * Find a shader by its signature.
     * @param String with shader signature
     * @returns -> Shader or NULL if not found
     */
    Shader* findShader(const std::string& signature);

    /*
     * Get a shader by its ShaderManager ID.
     * This ID is not the same as the native shader program ID.
     *
     * @param ID returned from addShader
     * @returns -> Shader or NULL if not found
     */
    Shader* getShader(int id);

    /*
     * Print signatures and IDS of all shaders to logcat
     */
    void dump();

private:
    ShaderManager(const ShaderManager& shader_manager);
    ShaderManager(ShaderManager&& shader_manager);
    ShaderManager& operator=(const ShaderManager& shader_manager);
    ShaderManager& operator=(ShaderManager&& shader_manager);

private:
    int latest_shader_id_ = 0;
    std::map<std::string, Shader*> shadersBySignature;
    std::map<int, Shader*> shadersByID;
    std::mutex lock_;
};

}
#endif
