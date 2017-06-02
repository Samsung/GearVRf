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
 * A shader which an user can add in run-time.
 ***************************************************************************/

#include "shader.h"

namespace gvr {

const bool Shader::LOG_SHADER = true;

Shader::Shader(int id,
               const std::string& signature,
               const std::string& uniformDescriptor,
               const std::string& textureDescriptor,
               const std::string& vertexDescriptor,
               const std::string& vertex_shader,
               const std::string& fragment_shader)
    : id_(id), signature_(signature),
      uniformDescriptor_(uniformDescriptor),
      textureDescriptor_(textureDescriptor),
      vertexDescriptor_(vertexDescriptor),
      vertexShader_(vertex_shader),
      fragmentShader_(fragment_shader),
      shaderDirty(true)  {

    if(vertex_shader.find("Transform_ubo") == std::string::npos && fragment_shader.find("Transform_ubo") == std::string::npos )
        transformUboPresent = false;

}
void Shader::parseDescriptor(const std::string& descriptor){
    if(textures_.size() != 0){
        return;
    }

    const char *p = descriptor.c_str();
    const char *type_start;
    int type_size;
    const char *name_start;
    int name_size;

    while (*p) {
        while (std::isspace(*p) || std::ispunct(*p))
            ++p;
        type_start = p;
        if (*p == 0)
            break;
        while (std::isalnum(*p))
            ++p;
        type_size = p - type_start;
        if (type_size == 0) {
            LOGE("SHADER: SYNTAX ERROR: expecting data type %s\n", descriptor.c_str());
            break;
        }
        std::string type(type_start, type_size);
        while (std::isspace(*p))
            ++p;
        name_start = p;
        while (std::isalnum(*p) || (*p == '_'))
            ++p;
        name_size = p - name_start;
        if (name_size == 0) {
            LOGE("SHADER: SYNTAX ERROR: expecting name\n");
            break;
        }
        std::string name(name_start, name_size);
        textures_[name] = type;
    }
}
void Shader::forEach(const std::string& descriptor, ShaderVisitor& visitor)
{
    if(textures_.size() != 0){
        for(auto it: textures_)
            visitor.visit(it.first, it.second, calcSize(it.second));
        return;
    }
    const char *p = descriptor.c_str();
    const char *type_start;
    int type_size;
    const char *name_start;
    int name_size;

    while (*p) {
        while (std::isspace(*p) || std::ispunct(*p))
            ++p;
        type_start = p;
        if (*p == 0)
            break;
        while (std::isalnum(*p))
            ++p;
        type_size = p - type_start;
        if (type_size == 0) {
            LOGE("SHADER: SYNTAX ERROR: expecting data type %s\n", descriptor.c_str());
            break;
        }
        std::string type(type_start, type_size);
        while (std::isspace(*p))
            ++p;
        name_start = p;
        while (std::isalnum(*p) || (*p == '_'))
            ++p;
        name_size = p - name_start;
        if (name_size == 0) {
            LOGE("SHADER: SYNTAX ERROR: expecting name\n");
            break;
        }
        std::string name(name_start, name_size);
        textures_[name] = type;
        visitor.visit(name, type, calcSize(type));
    }
}

int Shader::calcSize(std::string type)
{
    if (type == "float") return 1;
    if (type == "float3") return 3;
    if (type == "float4") return 4;
    if (type == "float2") return 2;
    if (type == "int") return 1;
    if (type == "int3") return 4;
    if (type == "int4") return 4;
    if (type == "float2") return 2;
    if (type == "mat4") return 16;
    if (type == "mat3") return 12;
    return 0;
}



} /* namespace gvr */
