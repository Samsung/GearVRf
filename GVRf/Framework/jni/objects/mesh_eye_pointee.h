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

#ifndef MESH_EYE_POINTEE_H_
#define MESH_EYE_POINTEE_H_

#include <memory>

#include "objects/eye_pointee.h"

namespace gvr {
class Mesh;

class MeshEyePointee: public EyePointee {
public:
    MeshEyePointee(Mesh* mesh);
    ~MeshEyePointee();

    Mesh* mesh() const {
        return mesh_;
    }

    void set_mesh(Mesh* mesh) {
        mesh_ = mesh;
    }

    EyePointData isPointed(const glm::mat4& mv_matrix);
    EyePointData isPointed(const glm::mat4& mv_matrix, float ox, float oy,
            float oz, float dx, float dy, float dz);
    static EyePointData isPointed(const Mesh& mesh, const glm::mat4& mv_matrix, float ox,
            float oy, float oz, float dx, float dy, float dz);

private:
    MeshEyePointee(const MeshEyePointee& mesh_eye_pointee);
    MeshEyePointee(MeshEyePointee&& mesh_eye_pointee);
    MeshEyePointee& operator=(const MeshEyePointee& mesh_eye_pointee);
    MeshEyePointee& operator=(MeshEyePointee&& mesh_eye_pointee);

private:
    Mesh* mesh_;
};
}
#endif
