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
 * Can hold eye pointees and attached to a scene object.
 ***************************************************************************/

#include "eye_pointee_holder.h"

#include "objects/scene_object.h"
#include "objects/eye_pointee.h"

namespace gvr {
EyePointeeHolder::EyePointeeHolder() :
        Component(), enable_(true), pointees_() {
}

EyePointeeHolder::~EyePointeeHolder() {
}

void EyePointeeHolder::addPointee(const std::shared_ptr<EyePointee>& pointee) {
    pointees_.push_back(pointee);
}

void EyePointeeHolder::removePointee(
        const std::shared_ptr<EyePointee>& pointee) {
    pointees_.erase(std::remove(pointees_.begin(), pointees_.end(), pointee),
            pointees_.end());
}

EyePointData EyePointeeHolder::isPointed(const glm::mat4& view_matrix, float ox,
        float oy, float oz, float dx, float dy, float dz) {
    glm::mat4 mv_matrix = view_matrix
            * owner_object()->transform()->getModelMatrix();
    EyePointData holder_data;
    for (auto it = pointees_.begin(); it != pointees_.end(); ++it) {
        EyePointData data = (*it)->isPointed(mv_matrix, ox, oy, oz, dx, dy, dz);
        if (data.distance() < holder_data.distance()) {
            holder_data = data;
        }
    }
    return holder_data;
}

EyePointData EyePointeeHolder::isPointed(const glm::mat4& view_matrix) {
    return isPointed(view_matrix, 0, 0, 0, 0, 0, -1);
}

}
