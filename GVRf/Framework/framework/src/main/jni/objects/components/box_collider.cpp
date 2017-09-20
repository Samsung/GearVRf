//
// Created by j.reynolds on 7/26/2017.
//
#include <limits>

#include "glm/glm.hpp"
#include "glm/gtc/matrix_inverse.hpp"
#include "box_collider.h"
#include "objects/scene_object.h"

namespace gvr {

ColliderData BoxCollider::isHit(const glm::vec3 &rayStart, const glm::vec3 &rayDir) {
    SceneObject* owner = owner_object();
    glm::vec3 O(rayStart);
    glm::vec3 D(rayDir);
    ColliderData colliderData;

    colliderData.ObjectHit = owner;
    colliderData.ColliderHit = this;

    if (owner != NULL) {
        glm::mat4 model_matrix = owner->transform()->getModelMatrix();
        glm::mat4 model_inverse = glm::affineInverse(model_matrix);

        transformRay(model_inverse, O, D);
    }

    glm::vec3 min_corner = -half_extents_;
    glm::vec3 max_corner = half_extents_;

    colliderData.IsHit = BoundingVolume::intersect(colliderData.HitPosition, O, D, min_corner, max_corner);

    colliderData.Distance = glm::distance(O, colliderData.HitPosition);
    return colliderData;
}

}
