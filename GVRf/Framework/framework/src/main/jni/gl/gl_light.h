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


#ifndef FRAMEWORK_GL_LIGHT_H
#define FRAMEWORK_GL_LIGHT_H

#include <string>

#include "objects/light.h"
#include "gl/gl_material.h"
#include "gl/gl_shader.h"

namespace gvr
{

/**
 * OpenGL implementation of Material which keeps uniform data
 * in a GLUniformBlock.
 */
    class GLLight : public Light
    {
    public:
        explicit GLLight(const char* uniform_desc, const char* texture_desc)
        :   Light(),
            uniforms_(uniform_desc, texture_desc, LIGHT_UBO_INDEX, "Lights_ubo")
            {
            }

        virtual ShaderData& uniforms()
        {
            return uniforms_;
        }

        void useGPUBuffer(bool flag)
        {
            uniforms_.useGPUBuffer(flag);
        }

        virtual const ShaderData& uniforms() const
        {
            return uniforms_;
        }

    protected:
        GLMaterial uniforms_;
    };
}

#endif //FRAMEWORK_GL_MATERIAL_H
