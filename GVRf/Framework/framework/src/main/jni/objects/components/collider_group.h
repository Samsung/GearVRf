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
 * Can hold a set of colliders attached to a scene object.
 ***************************************************************************/

#ifndef COLLIDE_GROUP_H_
#define COLLIDE_GROUP_H_

#include <algorithm>
#include <memory>
#include <vector>

#include "glm/glm.hpp"

#include "collider.h"

namespace gvr {

class ColliderGroup: public Collider {
public:
    ColliderGroup();
    ~ColliderGroup();

    void addCollider(Collider* collider);
    void removeCollider(Collider* collider);
    ColliderData isHit(const glm::vec3& rayStart, const glm::vec3& rayDir);

    const glm::vec3& hit() const {
        return hit_;
    }

private:
    ColliderGroup(const ColliderGroup& colliderGroup);
    ColliderGroup(ColliderGroup&& colliderGroup);
    ColliderGroup& operator=(const ColliderGroup& colliderGroup);
    ColliderGroup& operator=(ColliderGroup&& colliderGroup);

private:
    glm::vec3 hit_;
    std::vector<Collider*> colliders_;
};

}

#endif
