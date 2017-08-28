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
 * Collider made by a mesh.
 ***************************************************************************/

#ifndef MESH_COLLIDER_H_
#define MESH_COLLIDER_H_

#include <memory>

#include "collider.h"

namespace gvr {
class Mesh;
class BoundingVolume;

class MeshCollider: public Collider {
public:
    MeshCollider(Mesh* mesh = NULL);
    MeshCollider(Mesh* mesh, bool pickCoordinates);
    MeshCollider(bool useMeshBounds);
    ~MeshCollider();

    long shape_type() {
        return COLLIDER_SHAPE_MESH;
    }

    Mesh* mesh() const {
        return mesh_;
    }

    void set_mesh(Mesh* mesh) {
        mesh_ = mesh;
    }

    bool pickCoordinatesEnabled(){
        return pickCoordinates_;
    }

    ColliderData isHit(const glm::vec3& rayStart, const glm::vec3& rayDir);
    static ColliderData isHit(const BoundingVolume& bounds, const glm::vec3& rayStart, const glm::vec3& rayDir);

private:
    MeshCollider(const MeshCollider& mesh_collider);
    MeshCollider(MeshCollider&& mesh_collider);
    MeshCollider& operator=(const MeshCollider& mesh_collider);
    MeshCollider& operator=(MeshCollider&& mesh_collider);
    static ColliderData isHit(const Mesh& mesh, const glm::vec3& rayStart, const glm::vec3& rayDir, bool pickCoordinates);
    static float rayTriangleIntersect(glm::vec3& hitPos, const glm::vec3& rayStart, const glm::vec3& rayDir,
                               const glm::vec3& V1, const glm::vec3& V2, const glm::vec3& V3);
private:
    bool useMeshBounds_;
    bool pickCoordinates_;
    Mesh* mesh_;
};
}
#endif
