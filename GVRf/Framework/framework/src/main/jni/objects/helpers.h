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

#ifndef HELPERS_H_
#define HELPERS_H_

#include <memory>
#include <unordered_set>
enum DIRTY_BITS {
    STD_ATTRIBS = 0,
    CUSTOM_ATTRIBS,
    NEW_MATERIAL,
    MOD_CULL_FACE,
    MOD_SHADER_ID,
    NEW_TEXTURE,
    MOD_TEXTURE,
    MAT_DATA,
    NATIVE_SHADER,
    RENDER_PASS,
    NEW_MESH
};
namespace gvr {

static void dirtyImpl(std::unordered_set<std::shared_ptr<u_short>>& dirty_flags,DIRTY_BITS bit) {
    for (std::unordered_set<std::shared_ptr<u_short>>::iterator it = dirty_flags.begin();
         it != dirty_flags.end(); ++it) {
        const std::shared_ptr<u_short> &flag = *it;

        if (1 == flag.use_count()) {
            dirty_flags.erase(it);
            if (it == dirty_flags.end()) {
                break;
            }
        } else {
            *flag = *flag | ( 1 << bit);
        }
    }
}

}

#endif
