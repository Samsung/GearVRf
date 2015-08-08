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
 * RAII class for GL programs.
 ***************************************************************************/

#ifndef GL_PROGRAM_H_
#define GL_PROGRAM_H_

#include "GLES3/gl3.h"

#include "engine/memory/gl_delete.h"

#include "util/gvr_log.h"

namespace gvr {
class GLProgram {
public:
    GLProgram(const char* pVertexSourceStrings,
            const char* pFragmentSourceStrings) {
        GLint vertex_shader_string_lengths[1] = { (GLint) strlen(
                pVertexSourceStrings) };
        GLint fragment_shader_string_lengths[1] = { (GLint) strlen(
                pFragmentSourceStrings) };
        id_ = createProgram(1, &pVertexSourceStrings,
                vertex_shader_string_lengths, &pFragmentSourceStrings,
                fragment_shader_string_lengths);
    }

    GLProgram(const char** pVertexSourceStrings,
            const GLint* pVertexSourceStringLengths,
            const char** pFragmentSourceStrings,
            const GLint* pFragmentSourceStringLengths, int count) :
            id_(
                    createProgram(count, pVertexSourceStrings,
                            pVertexSourceStringLengths, pFragmentSourceStrings,
                            pFragmentSourceStringLengths)) {
    }

    ~GLProgram() {
        gl_delete.queueProgram(id_);
    }

    GLuint id() const {
        return id_;
    }

    static void checkGlError(const char* op) {
        for (GLint error = glGetError(); error; error = glGetError()) {
            LOGI("after %s() glError (0x%x)\n", op, error);
        }
    }

    static GLuint loadShader(GLenum shaderType, int strLength, const char** pSourceStrings,
            const GLint*pSourceStringLengths) {
        GLuint shader = glCreateShader(shaderType);
        if (shader) {
            glShaderSource(shader, strLength, pSourceStrings, pSourceStringLengths);
            glCompileShader(shader);
            GLint compiled = 0;
            glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
            if (!compiled) {
                GLint infoLen = 0;
                glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
                if (infoLen) {
                    char* buf = (char*) malloc(infoLen);
                    if (buf) {
                        glGetShaderInfoLog(shader, infoLen, NULL, buf);
                        LOGE("Could not compile shader %d:\n%s\n", shaderType,
                                buf);
                        free(buf);
                    }
                    gl_delete.queueShader(shader);
                    shader = 0;
                }
            }
        }
        return shader;
    }

    static GLuint createProgram(int strLength,
            const char** pVertexSourceStrings,
            const GLint* pVertexSourceStringLengths,
            const char** pFragmentSourceStrings,
            const GLint* pFragmentSourceStringLengths) {
        GLuint vertexShader = loadShader(GL_VERTEX_SHADER, strLength,
                pVertexSourceStrings, pVertexSourceStringLengths);
        if (!vertexShader) {
            return 0;
        }

        GLuint pixelShader = loadShader(GL_FRAGMENT_SHADER, strLength,
                pFragmentSourceStrings, pFragmentSourceStringLengths);
        if (!pixelShader) {
            return 0;
        }

        GLuint program = glCreateProgram();
        if (program) {
            glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            glLinkProgram(program);
            GLint linkStatus = GL_FALSE;
            glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
            if (linkStatus != GL_TRUE) {
                GLint bufLength = 0;
                glGetProgramiv(program, GL_INFO_LOG_LENGTH, &bufLength);
                if (bufLength) {
                    char* buf = (char*) malloc(bufLength);
                    if (buf) {
                        glGetProgramInfoLog(program, bufLength, NULL, buf);
                        LOGE("Could not link program:\n%s\n", buf);
                        free(buf);
                    }
                }
                gl_delete.queueProgram(program);
                program = 0;
            }
        }
        return program;
    }

private:
    GLuint id_;
};
}
#endif
