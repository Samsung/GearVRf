
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


#ifndef GL_UNIFORMBLOCK_H_
#define GL_UNIFORMBLOCK_H_

#include "objects/uniform_block.h"

namespace gvr
{

/**
 * Manages a Uniform Block containing data parameters to pass to
 * OpenGL vertex and fragment shaders.
 *
 * The UniformBlock may be updated by the application. If it has changed,
 * GearVRf resends the entire data block to OpenGL.
 */
    class GLUniformBlock : public UniformBlock
    {
    public:
        GLUniformBlock(const char *descriptor, int bindingPoint, const char *blockName);
        GLUniformBlock(const char *descriptor, int bindingPoint, const char *blockName, int maxelems);

        virtual ~GLUniformBlock();

        /**
         * Copy the data from CPU into the OpenGL uniform buffer.
         */
        virtual bool updateGPU(Renderer *);

        /*
         * Bind the uniform buffer to the OpenGL shader
         */
        virtual bool bindBuffer(Shader *, Renderer *);

        virtual std::string makeShaderLayout();
        virtual bool setFloatVec(const char *name, const float *val, int n);
        virtual bool setIntVec(const char *name, const int *val, int n);

        /**
         * Dump the contents of the shader uniforms to the log.
         * @param programID OpenGL program ID for shader
         * @param blockIndex OpenGL binding point
         */
        static void dump(GLuint programID, int blockIndex);

    protected:
        GLuint GLBuffer;
        GLuint GLOffset;
    };

}
#endif
