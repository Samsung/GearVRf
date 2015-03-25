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
#include <GLES3/gl3.h>
#include <GLES3/gl3ext.h>

#include "util/gvr_log.h"

#define _GVRF_USE_GLES3_  1

namespace gvr {

static void checkGlError(const char* op) {
    for (GLint error = glGetError(); error; error = glGetError()) {
        LOGI("after %s() glError (0x%x)\n", op, error);
    }
}

}
#endif
