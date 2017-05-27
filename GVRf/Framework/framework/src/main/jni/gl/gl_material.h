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


#ifndef FRAMEWORK_GL_UNIFORM_BLOCK_H
#define FRAMEWORK_GL_UNIFORM_BLOCK_H

#include "objects/shader_data.h"
#include "glm/glm.hpp"
#include "util/gvr_log.h"
#include "gl/gl_uniform_block.h"
#include "gl_image.h"
#include "gl_imagetex.h"
#include "gl/gl_shader.h"

namespace gvr
{

/**
 * OpenGL implementation of Material which keeps uniform data
 * in a GLUniformBlock.
 */
    class GLMaterial : public ShaderData
    {
    public:
        GLMaterial(const std::string &descriptor) : ShaderData(descriptor),
                                                    uniforms_(descriptor, MATERIAL_UBO_INDEX, "Material_ubo")
        {
        }

        virtual UniformBlock& uniforms()
        {
            return uniforms_;
        }

        virtual const UniformBlock& uniforms() const
        {
            return uniforms_;
        }

        virtual int bindToShader(Shader* shader);

    protected:
        GLUniformBlock uniforms_;

        int bindTextures(Shader *shader);
    };
}

#endif //FRAMEWORK_GL_UNIFORM_BLOCK_H
