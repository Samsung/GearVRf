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
#include "gl/gl_material.h"
#include "gl/gl_shader.h"

namespace gvr {
    GLUniformBlock::GLUniformBlock(const char* descriptor, int bindingPoint, const char* blockName)
      : UniformBlock(descriptor, bindingPoint, blockName),
        GLOffset(0),
        GLBuffer(0)
    { }

    GLUniformBlock::GLUniformBlock(const char* descriptor, int bindingPoint, const char* blockName, int maxelems)
      : UniformBlock(descriptor, bindingPoint, blockName, maxelems),
        GLOffset(0),
        GLBuffer(0)
    { }

    GLUniformBlock::~GLUniformBlock()
    {
        glDeleteBuffers(1,&GLBuffer);
    }

    bool GLUniformBlock::updateGPU(Renderer* unused)
    {
        if (mUseBuffer)             // use a uniform buffer?
        {
            if (mBindingPoint < 0)
            {
                return false;
            }
            if (GLBuffer == 0)
            {
                glGenBuffers(1, &GLBuffer);
                glBindBuffer(GL_UNIFORM_BUFFER, GLBuffer);
                glBufferData(GL_UNIFORM_BUFFER, mElemSize * mMaxElems, NULL, GL_DYNAMIC_DRAW);
                mIsDirty = true;
            }
            if (mIsDirty)
            {
                glBindBufferBase(GL_UNIFORM_BUFFER, mBindingPoint, GLBuffer);
                glBufferSubData(GL_UNIFORM_BUFFER, GLOffset, mElemSize * mMaxElems, getData());
                mIsDirty = false;
#ifdef DEBUG_SHADER
                LOGV("SHADER: UniformBlock::updateGPU %s size %d\n", getBlockName(), getTotalSize());
#endif
            }
            checkGLError("GLUniformBlock::updateGPU");
        }
        return true;
    }

    bool GLUniformBlock::bindBuffer(Shader* shader, Renderer* unused)
    {
        GLShader* glshader = static_cast<GLShader*>(shader);

        if (!mUseBuffer)
        {
            DataDescriptor::forEachEntry([this, glshader](const DataEntry& e) mutable
             {
                 if (!e.IsSet || e.NotUsed)
                 {
                     return;
                 }
                 int loc = glshader->getUniformLoc(e.Index, getBindingPoint());
                 if (loc < 0)
                 {
                     return;
                 }
                 int elemsize = e.Size / e.Count;
                 const char* data = static_cast<const char*>(getData());
                 data += e.Offset;
                 if (e.IsInt)
                 {
                     elemsize /= sizeof(int);
                     if (e.Type.compare("uint") == 0)
                     {
                         switch (elemsize)
                         {
                             case 1:
                             glUniform1uiv(loc, e.Count, (GLuint*) data);
                             break;

                             case 2:
                             glUniform2uiv(loc, e.Count, (GLuint*) data);
                             break;

                             case 3:
                             glUniform3uiv(loc, e.Count, (GLuint*) data);
                             break;

                            case 4:
                             glUniform4uiv(loc, e.Count, (GLuint*) data);
                             break;

                             default:
                             LOGE("UniformBlock: ERROR invalid integer vector size %d", elemsize);
                             break;
                         }
                     }
                     else
                     {
                         switch (elemsize)
                         {
                             case 1:
                             glUniform1iv(loc, e.Count, (const int*) data);
                             break;

                             case 2:
                             glUniform2iv(loc, e.Count, (const int*) data);
                             break;

                             case 3:
                             glUniform3iv(loc, e.Count, (const int*) data);
                             break;

                             case 4:
                             glUniform4iv(loc, e.Count, (const int*) data);
                             break;

                             default:
                             LOGE("UniformBlock: ERROR invalid integer vector size %d", elemsize);
                             break;
                     }
                     }

                 }
                 else if (e.IsMatrix)
                 {
                     elemsize /= sizeof(float);
                     switch (elemsize)
                     {
                         case 12:
                         glUniformMatrix3x4fv(loc, e.Count, false, (const float*) data);
                         break;

                         case 16:
                         glUniformMatrix4fv(loc, e.Count, false, (const float*) data);
                         break;

                         default:
                         LOGE("UniformBlock: ERROR invalid integer matrix size %d", elemsize);
                         break;
                     }
                 }
                 else
                 {
                     elemsize /= sizeof(float);
                     switch (elemsize)
                     {
                         case 1:
                         glUniform1fv(loc, e.Count, (const float*) data);
                         break;

                         case 2:
                         glUniform2fv(loc, e.Count, (const float*) data);
                         break;

                         case 3:
                         glUniform3fv(loc, e.Count, (const float*) data);
                         break;

                         case 4:
                         glUniform4fv(loc, e.Count, (const float*) data);
                         break;

                         default:
                         LOGE("UniformBlock: ERROR invalid float vector size %d", elemsize);
                         break;
                     }
                 }
                 checkGLError("GLUniformBlock::bindBuffer");
             });
        }
        else if (GLBuffer > 0)
        {
            GLuint blockIndex = glGetUniformBlockIndex(glshader->getProgramId(), getBlockName());
            glBindBuffer(GL_UNIFORM_BUFFER, GLBuffer);

            if (GL_INVALID_INDEX == blockIndex)
            {
                LOGE("UniformBlock: ERROR: cannot find block named %s\n", getBlockName());
                return false;
            }
            glUniformBlockBinding(glshader->getProgramId(), blockIndex, mBindingPoint);
            glBindBufferBase(GL_UNIFORM_BUFFER, mBindingPoint, GLBuffer);
#ifdef DEBUG_SHADER
            LOGV("SHADER: UniformBlock::bindBuffer %s bind at %d index = %d\n", getBlockName(),
                     mBindingPoint, blockIndex);
#endif
            checkGLError("GLUniformBlock::bindBuffer");
            return true;
        }
        return false;
    }

    std::string GLUniformBlock::makeShaderLayout()
    {
        return UniformBlock::makeShaderLayout();
    }

    void GLUniformBlock::dump(GLuint programID, int blockIndex)
    {
        // get size of name of the uniform block
        GLint nameLength;
        glGetActiveUniformBlockiv(programID, blockIndex, GL_UNIFORM_BLOCK_NAME_LENGTH, &nameLength);

        // get name of uniform block
        GLchar blockName[nameLength];
        glGetActiveUniformBlockName(programID, blockIndex, nameLength, NULL, blockName);

        // get size of uniform block in bytes
        GLint byteSize;
        glGetActiveUniformBlockiv(programID, blockIndex, GL_UNIFORM_BLOCK_DATA_SIZE, &byteSize);

        // get number of uniform variables in uniform block
        GLint numberOfUniformsInBlock;
        glGetActiveUniformBlockiv(programID, blockIndex,
                                  GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS, &numberOfUniformsInBlock);

        // get indices of uniform variables in uniform block
        GLint uniformsIndices[numberOfUniformsInBlock];
        glGetActiveUniformBlockiv(programID, blockIndex, GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES, uniformsIndices);
        LOGV("UniformBlock: %s %d bytes\n", blockName, byteSize);

        // get parameters of all uniform variables in uniform block
        for (int uniformMember=0; uniformMember<numberOfUniformsInBlock; uniformMember++)
        {
            if (uniformsIndices[uniformMember] > 0)
            {
                // index of uniform variable
                GLuint tUniformIndex = uniformsIndices[uniformMember];

                uniformsIndices[uniformMember];
                GLint uniformNameLength, uniformOffset, uniformSize;
                GLint uniformType, arrayStride, matrixStride;

                // get length of name of uniform variable
                glGetActiveUniformsiv(programID, 1, &tUniformIndex,
                                      GL_UNIFORM_NAME_LENGTH, &uniformNameLength);
                // get name of uniform variable
                GLchar uniformName[uniformNameLength];
                glGetActiveUniform(programID, tUniformIndex, uniformNameLength,
                                   NULL, NULL, NULL, uniformName);

                // get offset of uniform variable related to start of uniform block
                glGetActiveUniformsiv(programID, 1, &tUniformIndex,
                                      GL_UNIFORM_OFFSET, &uniformOffset);
                // get size of uniform variable (number of elements)
                glGetActiveUniformsiv(programID, 1, &tUniformIndex,
                                      GL_UNIFORM_SIZE, &uniformSize);
                // get type of uniform variable (size depends on this value)
                glGetActiveUniformsiv(programID, 1, &tUniformIndex,
                                      GL_UNIFORM_TYPE, &uniformType);
                // offset between two elements of the array
                glGetActiveUniformsiv(programID, 1, &tUniformIndex,
                                      GL_UNIFORM_ARRAY_STRIDE, &arrayStride);
                // offset between two vectors in matrix
                glGetActiveUniformsiv(programID, 1, &tUniformIndex,
                                      GL_UNIFORM_MATRIX_STRIDE, &matrixStride);
           }
        }
    }

    bool GLUniformBlock::setFloatVec(const char* name, const float *val, int n)
    {
        int bytesize = n * sizeof(float);
        char *data = getData(name, bytesize);
        if (data != NULL)
        {
            memcpy(data, val, bytesize);
            markDirty();
            return true;
        }
        return false;
    }

    bool GLUniformBlock::setIntVec(const char* name, const int *val, int n)
    {
        int bytesize = n * sizeof(int);
        char *data = getData(name, bytesize);
        if (data != NULL)
        {
            memcpy(data, val, bytesize);
            markDirty();
            return true;
        }
        return false;
    }

}