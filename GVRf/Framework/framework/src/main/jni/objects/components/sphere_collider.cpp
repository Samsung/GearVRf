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
#include "mesh_collider.h"

namespace gvr {
/*
 * Determine if the ray hits the collider.
 * @param rayStart      origin of ray in world coordinates
 * @param rayDir        direction of ray in world coordinates
 */
ColliderData SphereCollider::isHit(const glm::vec3& rayStart, const glm::vec3& rayDir)
{
    glm::vec3    sphCenter(0, 0, 0);
    float        radius = radius_;
    SceneObject* owner = owner_object();
    glm::mat4    model_matrix;

    /*
     * If we have a scene object with a mesh
     * get the sphere center and radius from that.
     */
    RenderData* rd = owner->render_data();
    Transform* t = owner->transform();
    if (t != NULL)
    {
        model_matrix = t->getModelMatrix();
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
            if (radius <= 0)
            {
                radius = meshbv.radius();
            }
        }
    }
    if (radius <= 0)
    {
        radius = 1;
    }
    ColliderData data = isHit(model_matrix, sphCenter, radius, rayStart, rayDir);
    data.ObjectHit = owner;
    data.ColliderHit = this;
    return data;
}

/*
 * Determine if the input sphere hits the sphere collider.
 * @param sphere  float array with center and radius of sphere
 *                in world coordinates.
 */
ColliderData SphereCollider::isHit(const float sphere[])
{
    ColliderData data;
    glm::vec3    colliderCenter(0, 0, 0);
    float        radius = radius_;
    SceneObject* owner = owner_object();
    glm::mat4    model_matrix;

    /*
     * If we have a scene object with a mesh
     * get the sphere center and radius from that.
     */
    RenderData* rd = owner->render_data();
    Transform* t = owner->transform();
    glm::mat4 model_inverse = glm::affineInverse(t->getModelMatrix());
    float s[4] = { sphere[0], sphere[1], sphere[2], sphere[3] };

    transformSphere(model_inverse, s);

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
            colliderCenter = meshbv.center();
            if (radius <= 0)
            {
                radius = meshbv.radius();
            }
        }
    }
    if (radius <= 0)
    {
        radius = 1;
    }
    glm::vec3 sphereCenter(s[0], s[1], s[2]);
    float r = s[3] + radius;
    glm::vec3 h = colliderCenter - sphereCenter;  // vector from collider to sphere
    float dist = (h.x * h.x) + (h.y * h.y) + (h.z * h.z);

    dist = sqrt(dist);
    if (dist <= r)                       // bounding sphere intersects collision sphere?
    {
        h *= radius / dist;          // hit point on collision sphere
        data.IsHit = true;
        data.ColliderHit = this;
        data.HitPosition = h;
        data.Distance = dist;
    }
    return data;
}

/*
 * Determine if the ray hits the collider.
 * @param model_matrix  matrix to transform model to world coordinates
 * @param Mesh          mesh to get origin and radius of collision sphere from
 * @param rayStart      origin of ray in world coordinates
 * @param rayDir        direction of ray in world coordinates
 */
ColliderData SphereCollider::isHit(Mesh& mesh, const glm::mat4& model_matrix, const glm::vec3& rayStart, const glm::vec3& rayDir)
{
    const BoundingVolume& meshbv = mesh.getBoundingVolume();

    return isHit(model_matrix, meshbv.center(), meshbv.radius(), rayStart, rayDir);
}

/*
 * Determine if the ray hits the collider.
 * @param model_matrix  matrix to transform model to world coordinates
 * @param radius        radius of collider in model coordinates
 * @param rayStart      origin of ray in world coordinates
 * @param rayDir        direction of ray in world coordinates
 */
ColliderData SphereCollider::isHit(const glm::mat4& model_matrix, const glm::vec3& center, float radius, const glm::vec3& rayStart, const glm::vec3& rayDir)
{
    ColliderData hitData;

    /*
     * Compute the inverse of the model view matrix and
     * apply it to the input ray. This puts it into the
     * same coordinate space as the mesh.
     */
    glm::vec3 start(rayStart);
    glm::vec3 dir(rayDir);
    glm::mat4 model_inverse = glm::affineInverse(model_matrix);

    transformRay(model_inverse, start, dir);
    /*
     * Compute the intersection of the ray and sphere in local coordinates.
     * The distance will be in world coordinates.
     */
    glm::vec3 hitPos;
    glm::vec3 hitNormal;

    if (glm::intersectRaySphere(start, dir, center, radius, hitPos, hitNormal))
    {
        glm::vec4 p = glm::vec4(hitPos, 1);

        hitData.IsHit = true;
        hitData.HitPosition = hitPos;
        p = model_matrix * p;
        hitData.Distance = glm::length(rayStart - glm::vec3(p));
    }
    return hitData;
}
}
