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
 * Shader for model loaded with Assimp
 ***************************************************************************/

#ifndef ASSIMP_SHADER_H_
#define ASSIMP_SHADER_H_

#include "shaderbase.h"

#define SETBIT(num, i)                   num = (num | (1 << i))
#define ISSET(num, i)                    ((num & (1 << i)) != 0)
#define CLEARBIT(num, i)                 num = (num & ~(1 << i))

// Indices of feature bits
#define AS_DIFFUSE_TEXTURE                0
#define AS_SPECULAR_TEXTURE               1
#define AS_SKINNING                       2

/*
 * As the features are incremented, need to increase AS_TOTAL_FEATURE_COUNT
 * as well.
 *
 * Also the AS_TOTAL_GL_PROGRAM_COUNT is the total number of combinations
 * possible with these feature set i.e for AS_TOTAL_FEATURE_COUNT = 3
 * AS_TOTAL_GL_PROGRAM_COUNT = 8
 *
 */
#define AS_TOTAL_FEATURE_COUNT            3
#define AS_TOTAL_GL_PROGRAM_COUNT         (1 << AS_TOTAL_FEATURE_COUNT)

namespace gvr {
class GLProgram;

class AssimpShader: public ShaderBase {
public:
    AssimpShader();
    virtual ~AssimpShader();

    virtual void render(RenderState* rstate,  RenderData* render_data,
            Material* material);

private:
    AssimpShader(const AssimpShader& assimp_shader);
    AssimpShader(AssimpShader&& assimp_shader);
    AssimpShader& operator=(const AssimpShader& assimp_shader);
    AssimpShader& operator=(AssimpShader&& assimp_shader);

private:
    GLProgram** program_list_;

    GLuint u_mvp_;
    GLuint u_texture_;
    GLuint u_diffuse_color_;
    GLuint u_ambient_color_;
    GLuint u_color_;
    GLuint u_opacity_;

    // Bones
    GLuint a_bone_indices_;
    GLuint a_bone_weights_;
    GLuint u_bone_matrices_;
};

}

#endif
