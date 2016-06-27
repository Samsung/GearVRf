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
 * Collider made from a sphere.
 ***************************************************************************/

#ifndef SPHERE_COLLIDER_H_
#define SPHERE_COLLIDER_H_

#include <memory>

#include "collider.h"
#include "objects/scene_object.h"

namespace gvr {

class SphereCollider: public Collider {
public:
    SphereCollider() :
        Collider(),
        center_(0, 0, 0),
        radius_(0) { }

    ~SphereCollider() { }

    void set_radius(float r)
    {
        radius_ = r;
    }

    float get_radius()
    {
        return radius_;
    }

    ColliderData isHit(const glm::mat4& view_matrix, const glm::vec3& rayStart, const glm::vec3& rayDir);
    static ColliderData isHit(Mesh& mesh, const glm::mat4& model_view, const glm::vec3& rayStart, const glm::vec3& rayDir);
    static ColliderData isHit(const glm::mat4& model_view, const glm::vec3& center, float radius, const glm::vec3& rayStart, const glm::vec3& rayDir);

private:
    SphereCollider(const SphereCollider& mesh_collider);
    SphereCollider(SphereCollider&& mesh_collider);
    SphereCollider& operator=(const SphereCollider& mesh_collider);
    SphereCollider& operator=(SphereCollider&& mesh_collider);

private:
    glm::vec3   center_;
    float       radius_;
};
}
#endif
