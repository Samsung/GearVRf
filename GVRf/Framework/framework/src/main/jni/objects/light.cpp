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
 * JNI
 ***************************************************************************/
#include <assert.h>
#include "light.h"
#include "util/gvr_gl.h"
#include "util/gvr_log.h"
#include <string>
#include <sstream>
#include <vector>

namespace gvr {


/*
 * Loads the uniforms associated with this light
 * into the GPU if they have changed.
 */
void Light::render(int program) {
    auto it = dirty_.find(program);

     if (it != dirty_.end() && !it->second)
        return;
    if (lightID_.empty()) {
        return;
    }
    dirty_[program] = false;
    std::string key;
    std::string lname = lightID_ + ".";
    std::stringstream ss;
    int offset;

    for (auto it = floats_.begin(); it != floats_.end(); ++it) {
        key = lname + it->first;
        offset = getOffset(it->first, program);
        if (offset <= 0) {
            offset = glGetUniformLocation(program, key.c_str());
            offsets_[it->first][program] = offset;
        }
        if (offset >= 0)
            glUniform1f(offset, it->second);
            LOGD("LIGHT: %d %s = %f\n", program, key.c_str(), it->second);
    }

    for (auto it = vec3s_.begin();
         it != vec3s_.end(); ++it) {
        offset = getOffset(it->first, program);
        key = lname + it->first;
        if (offset <= 0) {
            offset = glGetUniformLocation(program, key.c_str());
            offsets_[it->first][program] = offset;
          }
        if (offset >= 0) {
            glm::vec3 v = it->second;
            glUniform3f(offset, v.x, v.y, v.z);
            LOGD("LIGHT: %d %s = %f, %f, %f\n", program, key.c_str(), v.x, v.y, v.z);
        }
    }

    for (auto it = vec4s_.begin();
            it != vec4s_.end(); ++it) {
        offset = getOffset(it->first, program);
        key = lname + it->first;
        if (offset <= 0) {
            offset = glGetUniformLocation(program, key.c_str());
            offsets_[it->first][program] = offset;
        }
        if (offset >= 0) {
            glm::vec4 v = it->second;
            glUniform4f(offset, v.x, v.y, v.z, v.w);
            LOGD("LIGHT: %d %s = %f, %f, %f, %f\n", program, key.c_str(), v.x, v.y, v.z, v.w);
        }
    }
}
}
