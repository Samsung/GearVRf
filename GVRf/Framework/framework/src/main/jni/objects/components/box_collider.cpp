/***************************************************************************
 *  Collider made from a box.
 ***************************************************************************/

#include "glm/glm.hpp"
#include "glm/gtc/matrix_inverse.hpp"

#include "box_collider.h"
#include "render_data.h"
#include "objects/scene_object.h"

namespace gvr {
/*
 * Determine if the ray hits the collider.
 * @param owner         SceneObject that owns this collider or
 *                      the group it is part of.
 * @param rayStart      origin of ray in world coordinates
 * @param rayDir        direction of ray in world coordinates
 */
    ColliderData BoxCollider::isHit(SceneObject* owner, const glm::vec3& rayStart, const glm::vec3& rayDir)
    {
        glm::vec3    halfExtent(this->half_extents_);
        glm::mat4    model_matrix;

        /*
         * If we have a scene object with a mesh
         * get the box center and extents from that.
         */
        Transform* t = owner->transform();
        if (t != NULL)
        {
            model_matrix = t->getModelMatrix();
        }
        ColliderData data = isHit(model_matrix, halfExtent, rayStart, rayDir);
        data.ObjectHit = owner;
        data.ColliderHit = this;
        return data;
    }

    /*
     * Determine if the sphere hits the box.
     * @param sphere array with sphere center and radius
     */
    ColliderData BoxCollider::isHit(const glm::vec3& center, const glm::vec3& half_extents, const float sphere[])
    {
        glm::vec3       sphereCenter(sphere[0], sphere[1], sphere[2]);
        glm::vec3       rayDir(center - sphereCenter);
        glm::vec3       hitPos;
        ColliderData    data;
        BoundingVolume  box;

        box.expand(sphereCenter + half_extents); // box collider bounds
        box.expand(sphereCenter - half_extents);
        if (box.intersect(hitPos, sphereCenter, rayDir))
        {
            float dist = glm::length(sphereCenter - hitPos);

            if (dist <= sphere[3])
            {
                data.Distance = dist;
                data.IsHit = true;
                data.HitPosition = hitPos;      // hit position in box coordinates
            }
        }
        return data;
    }

    /*
     * Determine if the sphere hits the collider.
     * @param owner         SceneObject that owns this collider or
     *                      the group it is part of.
     * @param sphere array with sphere center and radius
     */
    ColliderData BoxCollider::isHit(SceneObject* owner, const float sphere[])
    {
        BoundingVolume& bounds = owner->getBoundingVolume();
        ColliderData data = BoxCollider::isHit(bounds.center(), half_extents_, sphere);
        data.ColliderHit = this;
        return data;
    }

/*
 * Determine if the ray hits the collider.
 * @param model_matrix  matrix to transform model to world coordinates
 * @param radius        radius of collider in model coordinates
 * @param rayStart      origin of ray in world coordinates
 * @param rayDir        direction of ray in world coordinates
 */
    ColliderData BoxCollider::isHit(const glm::mat4& model_matrix, const glm::vec3& half_extends, const glm::vec3& rayStart, const glm::vec3& rayDir)
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

        BoundingVolume *bounds = new BoundingVolume();
        bounds->expand(half_extends);
        bounds->expand(-half_extends);

        if (bounds->intersect(hitPos, start, dir))
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
