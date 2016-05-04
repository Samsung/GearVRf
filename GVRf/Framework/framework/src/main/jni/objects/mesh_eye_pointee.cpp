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
 * Eye pointee made by a mesh.
 ***************************************************************************/

#include "mesh_eye_pointee.h"

#include <limits>

#include "glm/glm.hpp"
#include "glm/gtc/matrix_inverse.hpp"

#include "objects/mesh.h"
#include "util/gvr_log.h"

namespace gvr {
MeshEyePointee::MeshEyePointee(Mesh* mesh) :
        EyePointee(), mesh_(mesh) {
}

MeshEyePointee::~MeshEyePointee() {
}

EyePointData MeshEyePointee::isPointed(const glm::mat4& mv_matrix, float ox,
        float oy, float oz, float dx, float dy, float dz) {
    if (nullptr != mesh_) {
        return isPointed(*mesh_, mv_matrix, ox, oy, oz, dx, dy, dz);
    } else {
        return EyePointData();
    }
}

EyePointData MeshEyePointee::isPointed(const Mesh& mesh,
        const glm::mat4& matrix, float ox, float oy, float oz, float dx,
        float dy, float dz) {
    glm::mat4 inv_mv_matrix = glm::affineInverse(matrix);
    std::vector<glm::vec4> relative_veritces;

    for (auto it = mesh.vertices().begin(); it != mesh.vertices().end();
            ++it) {
        glm::vec4 mesh_vertex(*it, 1.0f);
        relative_veritces.push_back(matrix * mesh_vertex);
    }

    EyePointData data;

    //http://en.wikipedia.org/wiki/M%C3%B6ller%E2%80%93Trumbore_intersection_algorithm
    for (int i = 0; i < mesh.triangles().size(); i += 3) {
        glm::vec3 O(ox, oy, oz);
        glm::vec3 D(dx, dy, dz);

        glm::vec3 V1(relative_veritces[mesh.triangles()[i]]);
        glm::vec3 V2(relative_veritces[mesh.triangles()[i + 1]]);
        glm::vec3 V3(relative_veritces[mesh.triangles()[i + 2]]);

        glm::vec3 e1(V2 - V1);
        glm::vec3 e2(V3 - V1);

        glm::vec3 P = glm::cross(D, e2);

        float det = glm::dot(e1, P);

        const float EPSILON = 0.00001f;

        if (det > -EPSILON && det < EPSILON) {
            continue;
        }

        float inv_det = 1.0f / det;

        glm::vec3 T(O - V1);

        float u = glm::dot(T, P) * inv_det;

        if (u < 0.0f || u > 1.0f) {
            continue;
        }

        glm::vec3 Q = glm::cross(T, e1);

        float v = glm::dot(D, Q) * inv_det;

        if (v < 0.0f || (u + v) > 1.0f) {
            continue;
        }

        float t = glm::dot(e2, Q) * inv_det;

        if (t > EPSILON) {
            float distance = t;
            if (distance < data.distance()) {
                data.setDistance(distance);
                data.setHit(
                        glm::vec3(
                                inv_mv_matrix
                                        * glm::vec4(
                                                (1.0f - u - v) * V1 + u * V2
                                                        + v * V3, 1.0f)));
            }
        }
    }

    return data;
}

EyePointData MeshEyePointee::isPointed(const glm::mat4& mv_matrix) {
    return isPointed(mv_matrix, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f);
}

}
