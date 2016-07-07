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
 * OpenGL related utility functions.
 ***************************************************************************/

#ifndef GL_UTIL_H_
#define GL_UTIL_H_

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <android/bitmap.h>
#include "util/gvr_jni.h"
#ifndef GL_ES_VERSION_3_0
#include <GLES3/gl3.h>
#include <GLES3/gl3ext.h>
#endif
#include "util/gvr_log.h"

namespace gvr {

static void checkGlError(const char* op) {
    for (GLint error = glGetError(); error; error = glGetError()) {
        LOGI("after %s() glError (0x%x)\n", op, error);
    }
}

static void dumpActiveAttribues(GLuint program)
{
    GLint numActiveAtributes;
    glGetProgramiv(program, GL_ACTIVE_ATTRIBUTES, &numActiveAtributes);
    GLchar attrName[512];
    LOGE("Attribute dump for Prog %d attr count -- %d", program, numActiveAtributes);
    for (int i = 0; i < numActiveAtributes; i++)
    {
        GLsizei length;
        GLint size;
        GLenum type;
        glGetActiveAttrib(program, i, 512, &length, &size, &type, attrName);
        LOGE(" Attr (%d) %s Location %d type %d size %d", i, attrName, glGetAttribLocation(program, attrName), (int) type, (int) size);
    }
}


static void dumpBufferData(GLfloat* buf, int stride, int length)
{
    char buffer[1024];
    int offset = 0;
    for (int i = 0; i < length; i++)
    {
        offset = 0;
        for (int j = 0; j < stride; j++)
        {
            offset += sprintf(buffer + offset, "(%d) %f ",i* stride + j, buf[i* stride + j]);
        }
        LOGE("%s", buffer);
    }
}

}
#endif
