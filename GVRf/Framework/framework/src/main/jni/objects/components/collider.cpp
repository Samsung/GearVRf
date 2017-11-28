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

namespace gvr
{

/*
 * Transform a ray in world coordinates to be in the coordinate space of a model.
 * @param model_matrix 4x4 matrix to transform model into world coordinates
 * @param rayStart origin of ray in world coordinates
 * @param rayDir directin of ray in world coordinates
 *
 * To put the ray in model coordinates it is pre-mulitplied by the input matrix
 */
    void
    Collider::transformRay(const glm::mat4 &model_matrix, glm::vec3 &rayStart, glm::vec3 &rayDir)
    {
        glm::vec4 start = model_matrix * glm::vec4(rayStart, 1);
        glm::vec4 end = model_matrix * glm::vec4(rayStart + rayDir, 1);

        rayDir = glm::vec3(end - start);
        rayDir = glm::normalize(rayDir);
        rayStart = glm::vec3(start);
    }

    void Collider::onAddedToScene(Scene *scene)
    {
        scene->addCollider(this);
    }

    void Collider::onRemovedFromScene(Scene *scene)
    {
        scene->removeCollider(this);
    }

/*
 * Transform a sphere in world coordinates to be in the coordinate space of a model.
 * @param model_matrix 4x4 matrix to transform model into world coordinates
 * @param sphere float array with center of sphere followed by radius
 * The new bounding sphere is returned in the original array.
 */
    void Collider::transformSphere(const glm::mat4 &model_matrix, float *sphere)
    {
        float radius = sphere[3];
        glm::vec4 corner1(sphere[0] - radius, sphere[1] - radius, sphere[2] - radius, 1);
        glm::vec4 corner2(sphere[0] + radius, sphere[1] + radius, sphere[2] + radius, 1);
        glm::vec4 c1 = model_matrix * corner1;
        glm::vec4 c2 = model_matrix * corner2;

        sphere[0] = (c1.x + c2.x) / 2;
        sphere[1] = (c1.y + c2.y) / 2;
        sphere[2] = (c1.z + c2.z) / 2;
        sphere[3] = glm::distance(glm::vec3(c1.x, c1.y, c1.z), glm::vec3(c2.x, c2.y, c2.z)) / 2.0f;
    }
}


