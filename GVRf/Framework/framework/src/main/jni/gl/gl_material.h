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
#include "gl_texture.h"
#include "shaders/material/custom_shader.h"

namespace gvr {
/**
 * OpenGL implementation of Material which keeps uniform data
 * in a GLUniformBlock.
 */
    class GLMaterial : public ShaderData
    {
    public:
        GLMaterial(const std::string& descriptor) : ShaderData(descriptor), uniforms_(descriptor,MATERIAL_UBO_INDEX) { }
        virtual UniformBlock& uniforms() { return uniforms_; }
        virtual const UniformBlock& uniforms() const { return uniforms_; }
        GLUniformBlock& getGLUniforms() { return uniforms_; }

        int render(Shader* shader)
        {
            std::lock_guard<std::mutex> lock(lock_);
            int texIndex = updateTextures(shader);
            if (texIndex >= 0)
            {
               // if (dirty_)
                {
                    updateUniforms(shader->getProgramId());
                    dirty_ = false;
                }
                return texIndex;
            }
            return -1;
        }

    protected:
        GLUniformBlock uniforms_;
        std::mutex lock_;

        void updateUniforms(int programID)
        {
            uniforms_.bindBuffer(programID);
            std::string s = uniforms_.toString();
            LOGD("SHADER: Material UBO");
            LOGD("SHADER: %s", s.c_str());
            uniforms_.render(programID);
        }

        bool updateTextures(Shader* shader)
        {
            int texIndex = 0;
            bool allOK = true;
            GLuint programID = shader->getProgramId();

            for (auto it = textures_.begin(); it != textures_.end(); ++it)
            {
                int loc = shader->getLocation(it->first);
                if (loc < 0)
                {
                    loc = glGetUniformLocation(programID, it->first.c_str());
                    if (loc < 0)
                    {
                        allOK = false;
                        continue;
                    }
                }
                GLTexture* tex = reinterpret_cast<GLTexture*>(it->second);
                if (tex && tex->bindTexture(texIndex, loc))
                {
                    ++texIndex;
                }
                else
                {
                    allOK = false;
                }
            }
            return allOK;
        }
    };
}

#endif //FRAMEWORK_GL_UNIFORM_BLOCK_H
