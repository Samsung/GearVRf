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
 * Collider made from a box.
 ***************************************************************************/

#ifndef BOX_COLLIDER_H_
#define BOX_COLLIDER_H_

#include "collider.h"

namespace gvr {

class BoxCollider : public Collider {
public:
    BoxCollider() :
        Collider(),
        half_extents_(0, 0, 0) { }

    ~BoxCollider() { }

    long shape_type() {
        return COLLIDER_SHAPE_BOX;
    }

    void set_half_extents(float x, float y, float z) {
        half_extents_ = glm::vec3(x, y, z);
    }

    glm::vec3 get_half_extents() {
        return half_extents_;
    }

    ColliderData isHit(const glm::vec3& rayStart, const glm::vec3& rayDir);
    ColliderData isHit(const glm::mat4& model_matrix, const glm::vec3& half_extends, const glm::vec3& rayStart, const glm::vec3& rayDir);

private:
    glm::vec3 half_extents_;
};

}

#endif
