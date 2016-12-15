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

namespace gvr {

/*
 * Transform a ray in world coordinates to be in the coordinate space of a model.
 * @param model_matrix 4x4 matrix to transform model into world coordinates
 * @param rayStart origin of ray in world coordinates
 * @param rayDir directin of ray in world coordinates
 *
 * To put the ray in model coordinates it is pre-mulitplied by the input matrix
 */
void Collider::transformRay(const glm::mat4& model_matrix, glm::vec3& rayStart, glm::vec3& rayDir)
{
    glm::vec4 start = model_matrix * glm::vec4(rayStart, 1);
    glm::vec4 end = model_matrix * glm::vec4(rayStart + rayDir, 1);

    rayDir = glm::vec3(end - start);
    rayDir = glm::normalize(rayDir);
    rayStart = glm::vec3(start);
}

void Collider::onAddedToScene(Scene* scene)
{
    scene->addCollider(this);
}

void Collider::onRemovedFromScene(Scene* scene)
{
    scene->removeCollider(this);
}

}
