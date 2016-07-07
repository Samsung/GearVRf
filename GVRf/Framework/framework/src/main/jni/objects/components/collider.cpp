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
 * Can hold multiple colliders attached to a scene object.
 ***************************************************************************/
#include "collider.h"
#include "glm/gtc/type_ptr.hpp"
#include "glm/gtc/matrix_inverse.hpp"
#include "objects/scene.h"
#include "objects/scene_object.h"

namespace gvr {

void Collider::transformRay(const glm::mat4& matrix, glm::vec3& rayStart, glm::vec3& rayDir)
{
    glm::mat4 mv_inverse = glm::affineInverse(matrix);
    glm::vec4 start = mv_inverse * glm::vec4(rayStart, 1);
    glm::vec4 dir(rayDir, 0);
    dir = mv_inverse * dir;
    rayDir = glm::vec3(glm::normalize(dir));
    rayStart = glm::vec3(start);
}


void Collider::set_owner_object(SceneObject* obj) {
    if (obj == owner_object())
    {
        return;
    }
    if (obj)
    {
        Scene::main_scene()->addCollider(this);
    }
    else
    {
        Scene::main_scene()->removeCollider(this);
    }
    Component::set_owner_object(obj);
}

}
