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
 * Can be picked by the picker.
 ***************************************************************************/

#ifndef COLLIDER_H_
#define COLLIDER_H_

#include <vector>
#include "glm/glm.hpp"

#include "collider_shape_types.h"
#include "objects/scene_object.h"

namespace gvr {
class Collider;

/*
 * Information from a collision when a collider is picked.
 */
class ColliderData
{
public:
    ColliderData(Collider* collider);
    ColliderData();

    void CopyHit(const ColliderData& src);

    SceneObject*    ObjectHit;
    Collider*       ColliderHit;
    bool            IsHit;
    glm::vec3       HitPosition;
    float           Distance;
    int             FaceIndex;
    int             CollidableIndex;
    glm::vec3       BarycentricCoordinates;
    glm::vec2       TextureCoordinates;
    glm::vec3       NormalCoordinates;
};

/*
 * Component attached to a SceneObject that provides
 * collision geometry and makes an object pickable.
 */
class Collider: public Component
{
public:
    virtual ~Collider() {}

    /*
     * Hit test the input ray against this collider.
     *
     * Casts the ray against the collider geometry and computes the hit
     * position (if any) in world space.
     *
     * @param owner       SceneObject which owns this collider.
     *                    If the collider is part of a group,
     *                    this will be the SceneObject which
     *                    owns the collider group.
     * @param rayStart    origin of the ray in world coordinates
     * @param rayDir      direction of the ray in world coordinates
     *
     * @returns ColliderData structure with hit point and distance from camera
     */
    virtual ColliderData isHit(SceneObject* owner, const glm::vec3& rayStart, const glm::vec3& rayDir) = 0;

    /*
     * Hit test the input sphere against this collider.
     *
     * Compares the center of the sphere against the bounding box of
     * the collider and registers a hit if the box is within the sphere.
     *
     * @param sphere    float array with center of sphere and radius
     *
     * @returns ColliderData structure with hit point and distance from camera
     */
    virtual ColliderData isHit(SceneObject* owner, const float sphere[]) = 0;

    virtual long shape_type() {
        return COLLIDER_SHAPE_UNKNOWN;
    }

    static long long getComponentType() {
        return COMPONENT_TYPE_COLLIDER;
    }

    void set_pick_distance(float dist) {
        pick_distance_ = dist;
    }

    float pick_distance() const {
        return pick_distance_;
    }
    static void transformRay(const glm::mat4& matrix, glm::vec3& rayStart, glm::vec3& rayDir);
    virtual void onAddedToScene(Scene* scene);
    virtual void onRemovedFromScene(Scene* scene);
    static void transformSphere(const glm::mat4& model_matrix, float* sphere);

protected:
    Collider() : Component(Collider::getComponentType()), pick_distance_(0) {}
    explicit Collider(long long type) : Component(type), pick_distance_(0) {}

    float pick_distance_;

    Collider(const Collider& collider) = delete;
    Collider(Collider&& collider) = delete;
    Collider& operator=(const Collider& collider) = delete;
    Collider& operator=(Collider&& collider) = delete;
};

inline ColliderData::ColliderData(Collider* collider) :
        ColliderHit(collider),
        IsHit(false),
        CollidableIndex(-1),
        HitPosition(std::numeric_limits<float>::infinity()),
        Distance((std::numeric_limits<float>::infinity())),
        FaceIndex(-1),
        BarycentricCoordinates(-1.0f),
        TextureCoordinates(-1.0f),
        NormalCoordinates(0.0f)
{
    if (collider != NULL)
    {
        ObjectHit = collider->owner_object();
    }
    else
    {
        ObjectHit = NULL;
    }
}

inline ColliderData::ColliderData() :
        ColliderHit(NULL),
        ObjectHit(NULL),
        IsHit(false),
        CollidableIndex(-1),
        HitPosition(std::numeric_limits<float>::infinity()),
        Distance(std::numeric_limits<float>::infinity()),
        FaceIndex(-1),
        BarycentricCoordinates(-1.0f),
        TextureCoordinates(-1.0f),
        NormalCoordinates(0.0f)
{
}

inline void ColliderData::CopyHit(const ColliderData& src)
{
    IsHit = src.IsHit;
    HitPosition = src.HitPosition;
    Distance = src.Distance;
}

inline bool compareColliderData(ColliderData i, ColliderData j) {
    return i.Distance < j.Distance;
}

}
#endif
