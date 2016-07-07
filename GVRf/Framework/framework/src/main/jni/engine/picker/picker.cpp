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
 * Picks scene object in a scene.
 ***************************************************************************/

#include "picker.h"

#include <limits>
#include <sstream>
#include "glm/glm.hpp"
#include "glm/gtc/matrix_inverse.hpp"

#include "objects/scene.h"
#include "objects/scene_object.h"
#include "objects/components/camera_rig.h"
#include "objects/components/render_data.h"
#include "objects/components/mesh_collider.h"

namespace gvr {

Picker::Picker() {
}

Picker::~Picker() {
}

/*
 * Intersects all the colliders in the scene with the input ray
 * and returns the list of collisions.
 *
 * This function is not thread-safe because it relies on a static
 * array of colliders which could be updated by a different thread.
 */
void Picker::pickScene(Scene* scene, std::vector<ColliderData>& picklist, float ox,
        float oy, float oz, float dx, float dy, float dz) {
    const std::vector<Component*>& colliders = scene->lockColliders();
    Transform* const t = scene->main_camera_rig()->getHeadTransform();

    if (nullptr != t) {
        glm::mat4 view_matrix = glm::affineInverse(t->getModelMatrix());

        for (auto it = colliders.begin(); it != colliders.end(); ++it) {
            Collider* collider = reinterpret_cast<Collider*>(*it);
            SceneObject* owner = collider->owner_object();
            if (collider->enabled() && (owner != NULL) && owner->enabled()) {
                ColliderData data = collider->isHit(view_matrix, glm::vec3(ox, oy, oz), glm::vec3(dx, dy, dz));
                if ((collider->pick_distance() > 0) && (collider->pick_distance() < data.Distance)) {
                    data.IsHit = false;
                }
                 if (data.IsHit) {
                    picklist.push_back(data);
                }
            }
        }
        std::sort(picklist.begin(), picklist.end(), compareColliderData);
    }
    scene->unlockColliders();
 }

void Picker::pickScene(Scene* scene, std::vector<ColliderData>& pickList) {
    pickScene(scene, pickList, 0, 0, 0, 0, 0, -1.0f);
}

float Picker::pickSceneObject(const SceneObject* scene_object,
        const CameraRig* camera_rig) {

    if (scene_object->collider() != 0) {
        Collider* collider = scene_object->collider();
        if (collider->enabled()) {
            glm::mat4 view_matrix = glm::affineInverse(camera_rig->getHeadTransform()->getModelMatrix());
            ColliderData data = collider->isHit(view_matrix);
            return data.Distance;
        }
    }
    return std::numeric_limits<float>::infinity();
}

/*
 * Pick against the scene bounding box.
 * The input ray is in world coordinates.
 * To pick against the bounding box, we create a bounding box mesh
 * from the original mesh. This new mesh is in mesh coordinates
 * so we must apply the inverse of the model matrix from the scene object
 * to the ray to put it into mesh coordinates.
 */
glm::vec3 Picker::pickSceneObjectAgainstBoundingBox(
        const SceneObject* scene_object, float ox, float oy, float oz, float dx,
        float dy, float dz) {
    RenderData* rd = scene_object->render_data();

    if ((rd == NULL) || (rd->mesh() == NULL)) {
        return glm::vec3(std::numeric_limits<float>::infinity());
    }
    glm::mat4 model_matrix = scene_object->transform()->getModelMatrix();
    std::unique_ptr<Mesh> mesh(rd->mesh()->createBoundingBox());
    glm::vec3 rayStart(ox, oy, oz);
    glm::vec3 rayDir(dx, dy, dz);

    glm::normalize(rayDir);
    ColliderData data = MeshCollider::isHit(*mesh, model_matrix, rayStart, rayDir);
    if (data.IsHit) {
        return data.HitPosition;
    }
    return glm::vec3(std::numeric_limits<float>::infinity());
}

}
