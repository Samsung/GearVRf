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
        ColliderData hitData;
        glm::vec3    halfExtent(this->half_extents_);
        glm::mat4    model_matrix;
        glm::vec3    hitPos;
        BoundingVolume bounds;
        Transform* t = owner->transform();

        hitData.ObjectHit = owner;
        hitData.ColliderHit = this;
        if (glm::length(halfExtent) == 0)
        {
            bounds = owner->getBoundingVolume();
            const glm::vec3& corner1(bounds.min_corner());
            const glm::vec3& corner2(bounds.max_corner());
            halfExtent.x = fabs(corner2.x - corner1.x);
            halfExtent.y = fabs(corner2.y - corner1.y);
            halfExtent.z = fabs(corner2.z - corner1.z);
            if (bounds.intersect(hitPos, rayStart, rayDir))
            {
                hitData.IsHit = true;
                hitData.HitPosition = hitPos;
                hitData.Distance = glm::length(rayStart - hitPos);
            }
        }
        else
        {
            glm::vec3 start(rayStart);
            glm::vec3 dir(rayDir);
            glm::mat4 model_inverse = glm::affineInverse(t->getModelMatrix());
            transformRay(model_inverse, start, dir);
            bounds.expand(halfExtent);
            bounds.expand(-halfExtent);
            if (bounds.intersect(hitPos, start, dir))
            {
                glm::vec4 p = glm::vec4(hitPos, 1);

                hitData.IsHit = true;
                hitData.HitPosition = hitPos;
                p = model_matrix * p;
                hitData.Distance = glm::length(start - glm::vec3(p));
            }
        }
        return hitData;
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

}
