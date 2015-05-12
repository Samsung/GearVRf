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

#ifndef CUSTOM_SHADER_H_
#define CUSTOM_SHADER_H_

#include <map>
#include <memory>
#include <string>

#include "GLES3/gl3.h"
#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"

#include "objects/eye_type.h"
#include "objects/recyclable_object.h"

namespace gvr {

class GLProgram;
class RenderData;

class CustomShader: public RecyclableObject {
public:
    explicit CustomShader(std::string vertex_shader,
            std::string fragment_shader);
    ~CustomShader();
    void recycle();
    void addTextureKey(std::string variable_name, std::string key);
    void addAttributeFloatKey(std::string variable_name, std::string key);
    void addAttributeVec2Key(std::string variable_name, std::string key);
    void addAttributeVec3Key(std::string variable_name, std::string key);
    void addAttributeVec4Key(std::string variable_name, std::string key);
    void addUniformFloatKey(std::string variable_name, std::string key);
    void addUniformVec2Key(std::string variable_name, std::string key);
    void addUniformVec3Key(std::string variable_name, std::string key);
    void addUniformVec4Key(std::string variable_name, std::string key);
    void addUniformMat4Key(std::string variable_name, std::string key);
    void render(const glm::mat4& mvp_matrix, RenderData* render_data, bool right);
    static int getGLTexture(int n);

private:
    CustomShader(const CustomShader& custom_shader);
    CustomShader(CustomShader&& custom_shader);
    CustomShader& operator=(const CustomShader& custom_shader);
    CustomShader& operator=(CustomShader&& custom_shader);

private:
    GLProgram* program_;
    GLuint a_position_;
    GLuint a_normal_;
    GLuint a_tex_coord_;
    GLuint u_mvp_;
    GLuint u_right_;
    std::map<int, std::string> texture_keys_;
    std::map<int, std::string> attribute_float_keys_;
    std::map<int, std::string> attribute_vec2_keys_;
    std::map<int, std::string> attribute_vec3_keys_;
    std::map<int, std::string> attribute_vec4_keys_;
    std::map<int, std::string> uniform_float_keys_;
    std::map<int, std::string> uniform_vec2_keys_;
    std::map<int, std::string> uniform_vec3_keys_;
    std::map<int, std::string> uniform_vec4_keys_;
    std::map<int, std::string> uniform_mat4_keys_;
};

}
#endif
