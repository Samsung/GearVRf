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
#include "glm/glm.hpp"
#include "glm/gtc/matrix_inverse.hpp"

#include "objects/scene.h"
#include "objects/scene_object.h"
#include "objects/components/camera_rig.h"
#include "objects/components/perspective_camera.h"
#include "objects/components/render_data.h"
#include "objects/components/mesh_collider.h"

namespace gvr {

Picker::Picker() {
}

Picker::~Picker() {
}

/**
 * Efficient means of solving Barycentric coordinates by Christer Ericson/John Calsbeek found at
 * https://gamedev.stackexchange.com/questions/23743/whats-the-most-efficient-way-to-find-barycentric-coordinates
 * @param p         3D point lying on triangle formed by points a, b, and c.
 * @param a         the first of the three points forming the triangle
 * @param b         the second of the three points forming the triangle
 * @param c         the third of the three points forming the triangle
 * @param coords    the vec3 that will hold the resulting Barcentric coordinates of p
 */
static void calcBarycentric(const glm::vec3 &p, const glm::vec3 &a, const glm::vec3 &b, const glm::vec3 &c, glm::vec3 &coords)
{
    glm::vec3 v0 = b - a, v1 = c - a, v2 = p - a;
    float d00 = glm::dot(v0, v0);
    float d01 = glm::dot(v0, v1);
    float d11 = glm::dot(v1, v1);
    float d20 = glm::dot(v2, v0);
    float d21 = glm::dot(v2, v1);
    float denom = d00 * d11 - d01 * d01;
    coords.y = (d11 * d20 - d01 * d21) / denom;
    coords.z = (d00 * d21 - d01 * d20) / denom;
    coords.x = 1.0f - coords.y - coords.z;
}

/**
 * Sets the Barycentric coordinates corresponding to the HitPoint on the mesh
 * @param mesh          the Mesh of the object that was collided with
 * @param colliderData  the ColliderData holding the HitPoint which will also store the Barycentric coordinates
 */
static void populateBarycentricCoords(const Mesh& mesh, ColliderData& colliderData) {
    const std::vector<glm::vec3> &vertices = mesh.vertices();

    glm::vec3 v1(vertices[mesh.triangles()[colliderData.FaceIndex * 3]]);
    glm::vec3 v2(vertices[mesh.triangles()[colliderData.FaceIndex * 3 + 1]]);
    glm::vec3 v3(vertices[mesh.triangles()[colliderData.FaceIndex * 3 + 2]]);

    calcBarycentric(colliderData.HitPosition, v1, v2, v3, colliderData.BarycentricCoordinates);
}

/**
 * Sets the Barycentric and UV coordinates corresponding to the HitPoint on the mesh
 * @param mesh          the Mesh of the object that was collided with
 * @param colliderData  the ColliderData holding the HitPoint which will also store the UV coordinates
 */
static void populateTexCoords(const Mesh& mesh, ColliderData& colliderData) {
    populateBarycentricCoords(mesh, colliderData);
    try{
        const std::vector<glm::vec2> &texCoords = mesh.getVec2Vector("a_texcoord"); //may not exist
        glm::vec2 u1(texCoords[mesh.triangles()[colliderData.FaceIndex * 3]]);
        glm::vec2 u2(texCoords[mesh.triangles()[colliderData.FaceIndex * 3 + 1]]);
        glm::vec2 u3(texCoords[mesh.triangles()[colliderData.FaceIndex * 3 + 2]]);

        colliderData.TextureCoordinates =   u1 * colliderData.BarycentricCoordinates.x
                                            + u2 * colliderData.BarycentricCoordinates.y
                                            + u3 * colliderData.BarycentricCoordinates.z;
    }
    catch (const std::string& warning){
        LOGW("%s", warning.c_str());
    }
    catch (...){
        LOGE("An unexpected error occurred while calculating texture coordinates.");
    }
}

/*
 * Intersects all the colliders in the scene with the input ray
 * and returns the list of collisions.
 *
 * This function is not thread-safe because it relies on a static
 * array of colliders which could be updated by a different thread.
 */
void Picker::pickScene(Scene* scene, std::vector<ColliderData>& picklist, Transform* t,
                       float ox, float oy, float oz, float dx, float dy, float dz) {
    glm::vec3 ray_start(ox, oy, oz);
    glm::vec3 ray_dir(dx, dy, dz);
    const std::vector<Component*>& colliders = scene->lockColliders();
    const glm::mat4& model_matrix = t->getModelMatrix();

    Collider::transformRay(model_matrix, ray_start, ray_dir);
    for (auto it = colliders.begin(); it != colliders.end(); ++it) {
        Collider* collider = reinterpret_cast<Collider*>(*it);
        SceneObject* owner = collider->owner_object();
        if (collider->enabled() && (owner != NULL) && owner->enabled()) {
            ColliderData data = collider->isHit(ray_start, ray_dir);
            if ((collider->pick_distance() > 0) && (collider->pick_distance() < data.Distance)) {
                data.IsHit = false;
            }
            if (data.IsHit) {
                // In order, we check that the object was hit,  that the collider-shape is a mesh, and that
                // the face-index is valid to ensure bounding-collisions aren't being used with the mesh-collider
                if(collider->shape_type() == COLLIDER_SHAPE_MESH && data.FaceIndex != -1) {
                    populateTexCoords(*owner->render_data()->mesh(), data);
                }
                picklist.push_back(data);
            }
        }
    }
    std::sort(picklist.begin(), picklist.end(), compareColliderData);
    scene->unlockColliders();
}

void Picker::pickScene(Scene* scene, std::vector<ColliderData>& pickList) {
    Transform* t = scene->main_camera_rig()->getHeadTransform();
    pickScene(scene, pickList, t, 0, 0, 0, 0, 0, -1.0f);
}

/**
 * Picks a single scene object from the scene. If the object has a mesh collider, the picker will calculate the
 * texture coordinates and barycentric coordinates of the corresponding hit-point. Note that this will do nothing
 * if the scene object doesn't have a collider.
 */
void Picker::pickSceneObject(const SceneObject *scene_object, float ox, float oy, float oz, float dx, float dy, float dz, ColliderData &colliderData){
    Collider* collider = scene_object->collider();
    if(collider == nullptr){
        return;
    }
    else if (collider->enabled() && scene_object->enabled()) {
        glm::vec3 rayStart(ox, oy, oz);
        glm::vec3 rayDir(dx, dy, dz);

        colliderData = collider->isHit(rayStart, rayDir);
        // In order, we check that the object was hit,  that the collider-shape is a mesh, and that
        // the face-index is valid to ensure bounding-collisions aren't being used with the mesh-collider
        if(colliderData.IsHit && collider->shape_type() == COLLIDER_SHAPE_MESH && colliderData.FaceIndex != -1){
            populateTexCoords(*scene_object->render_data()->mesh(), colliderData);
        }
    }
}
/*
 * Pick against the scene bounding box.
 * The input ray is in world coordinates.
 * To pick against the bounding box, we create a bounding box mesh
 * from the original mesh. This new mesh is in mesh coordinates
 * so we must apply the inverse of the model matrix from the scene object
 * to the ray to put it into mesh coordinates.
 */
glm::vec3 Picker::pickSceneObjectAgainstBoundingBox(const SceneObject* scene_object, float ox, float oy, float oz, float dx, float dy, float dz) {
    RenderData* rd = scene_object->render_data();

    if ((rd == NULL) || (rd->mesh() == NULL)) {
        return glm::vec3(std::numeric_limits<float>::infinity());
    }
    glm::mat4 model_inverse = glm::affineInverse(scene_object->transform()->getModelMatrix());
    const BoundingVolume& bounds = rd->mesh()->getBoundingVolume();
    glm::vec3 rayStart(ox, oy, oz);
    glm::vec3 rayDir(dx, dy, dz);

    glm::normalize(rayDir);
    Collider::transformRay(model_inverse, rayStart, rayDir);
    ColliderData data = MeshCollider::isHit(bounds, rayStart, rayDir);
    if (data.IsHit) {
        return data.HitPosition;
    }
    return glm::vec3(std::numeric_limits<float>::infinity());
}

/*
 * Returns the list of all visible colliders.
 *
 * This function is not thread-safe because it relies on a static
 * array of colliders which could be updated by a different thread.
 */
void Picker::pickVisible(Scene* scene, Transform* t, std::vector<ColliderData>& picklist) {
    const std::vector<Component*>& colliders = scene->lockColliders();

    for (auto it = colliders.begin(); it != colliders.end(); ++it) {
        Collider* collider = reinterpret_cast<Collider*>(*it);
        SceneObject* owner = collider->owner_object();
        if (collider->enabled() && (owner != NULL) && owner->enabled()) {
            ColliderData data(collider);
            Transform* trans = owner->transform();
            glm::mat4 worldmtx = trans->getModelMatrix();
            data.HitPosition = glm::vec3(worldmtx[3]);
            data.Distance = data.HitPosition.length();
            data.IsHit = true;
            // In order, we check that the object was hit,  that the collider-shape is a mesh, and that
            // the face-index is valid to ensure bounding-collisions aren't being used with the mesh-collider
            if(collider->shape_type() == COLLIDER_SHAPE_MESH && data.FaceIndex != -1){
                populateTexCoords(*owner->render_data()->mesh(), data);
            }
            picklist.push_back(data);
        }
    }
    std::sort(picklist.begin(), picklist.end(), compareColliderData);
    scene->unlockColliders();
}
}
