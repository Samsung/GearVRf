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

#include <limits>
#include "glm/gtx/intersect.hpp"
#include "glm/gtx/component_wise.hpp"
#include "glm/gtc/type_ptr.hpp"
#include "glm/gtc/matrix_inverse.hpp"

#include "util/gvr_log.h"

#include "sphere_collider.h"
#include "objects/mesh.h"

namespace gvr {
ColliderData SphereCollider::isHit(const glm::mat4& view_matrix, const glm::vec3& rayStart, const glm::vec3& rayDir)
{
    glm::vec3    sphCenter(0, 0, 0);
    float        radius = (radius_ > 0) ? radius_ : 1;
    SceneObject* owner = owner_object();
    glm::mat4    model_view(view_matrix);

    /*
     * If we have a scene object with a mesh
     * get the sphere center and radius from that.
     */
    if (owner != NULL)
    {
        RenderData* rd = owner->render_data();
        Transform* t = owner->transform();
        if (t != NULL)
        {
            model_view *= t->getModelMatrix();
        }
        /*
         * If there is a mesh attached to the scene object
         * use the bounding sphere of the mesh to compute
         * the sphere center and radius in mesh coordinates.
         */
        if (rd != NULL)
        {
            Mesh* mesh = rd->mesh();
            if (mesh != NULL)
            {
                const BoundingVolume& meshbv = mesh->getBoundingVolume();
                sphCenter = meshbv.center();
                radius = meshbv.radius();
            }
        }
    }
    ColliderData data = isHit(model_view, sphCenter, radius, rayStart, rayDir);
    data.ObjectHit = owner;
    data.ColliderHit = this;
    return data;
}

ColliderData SphereCollider::isHit(Mesh& mesh, const glm::mat4& model_view, const glm::vec3& rayStart, const glm::vec3& rayDir)
{
    const BoundingVolume& meshbv = mesh.getBoundingVolume();

    return isHit(model_view, meshbv.center(), meshbv.radius(), rayStart, rayDir);
}

ColliderData SphereCollider::isHit(const glm::mat4& model_view, const glm::vec3& center, float radius, const glm::vec3& rayStart, const glm::vec3& rayDir)
{
    ColliderData hitData;

    /*
     * Compute the inverse of the model view matrix and
     * apply it to the input ray. This puts it into the
     * same coordinate space as the mesh.
     */
    glm::vec3 start(rayStart);
    glm::vec3 dir(rayDir);
    transformRay(model_view, start, dir);

    /*
     * Compute the intersection of the ray and sphere in local coordinates.
     * The distance will be in world coordinates.
     */
    glm::vec3 hitPoint;
    glm::vec3 hitNormal;

    if (glm::intersectRaySphere(start, dir, center, radius, hitPoint, hitNormal))
    {
        glm::vec4 p = glm::vec4(hitPoint, 1);

        hitData.IsHit = true;
        hitData.HitPosition = hitPoint;
        p = model_view * p;
        hitData.Distance = glm::length(rayStart - glm::vec3(p));
    }
    return hitData;
}
}
